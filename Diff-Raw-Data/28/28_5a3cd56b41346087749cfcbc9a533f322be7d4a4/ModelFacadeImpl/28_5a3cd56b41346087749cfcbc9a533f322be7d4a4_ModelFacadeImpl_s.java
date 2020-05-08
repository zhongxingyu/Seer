 package ru.aifgi.recognizer.model;
 
 /*
  * Copyright 2012 Alexey Ivanov
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import ru.aifgi.recognizer.api.ImageWrapper;
 import ru.aifgi.recognizer.api.ModelFacade;
 import ru.aifgi.recognizer.api.ProgressListener;
 import ru.aifgi.recognizer.api.Rectangle;
 import ru.aifgi.recognizer.api.neural_network.NeuralNetwork;
 import ru.aifgi.recognizer.api.neural_network.TrainingElement;
 import ru.aifgi.recognizer.api.neural_network.TrainingResult;
 import ru.aifgi.recognizer.api.neural_network.TrainingSet;
 import ru.aifgi.recognizer.model.neural_network.NeuralNetworkImpl;
 import ru.aifgi.recognizer.model.neural_network.NeuralNetworkStructureImpl;
 import ru.aifgi.recognizer.model.neural_network.TrainingElementImpl;
 import ru.aifgi.recognizer.model.neural_network.TrainingSetImpl;
 import ru.aifgi.recognizer.model.preprosessing.Binarizer;
 import ru.aifgi.recognizer.model.preprosessing.ImageComponentsFinder;
 import ru.aifgi.recognizer.model.thread_factories.MyThreadFactory;
 
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 /**
  * @author aifgi
  */
 
 class ModelFacadeImpl implements ModelFacade {
     private final ExecutorService myExecutorService = Executors.newCachedThreadPool(new MyThreadFactory("Recognition"));
 
     private final Set<ProgressListener> myProgressListeners = new HashSet<>();
     private NeuralNetwork myNeuralNetwork;
     private Labels myLabels;
 
     @Override
     public void addProgressListener(final ProgressListener progressListener) {
         myProgressListeners.add(progressListener);
     }
 
     @Override
     public void removeProgressListener(final ProgressListener progressListener) {
         myProgressListeners.remove(progressListener);
     }
 
     @Override
     public String recognize(final ImageWrapper inputImage) {
         notifyStarted("Recognition started");
 
         final String result = doRecognize(inputImage);
 
         notifyDone("Recognition done");
         return result;
     }
 
     private String doRecognize(final ImageWrapper inputImage) {
         init();
         double[][] input = inputImage.getBrightnesses();
         notifyProgress(1, "Binarization");
         input = new Binarizer().apply(input);
         notifyProgress(5, "Find connected components");
         final ImageComponentsFinder imageComponentsFinder = new ImageComponentsFinder(input);
         final Collection<Rectangle> words = imageComponentsFinder.getWords();
         notifyProgress(20, "Word components recognition");
 
         final String[] recognizedWords = recognizeWords(input, words);
 
         return buildResult(recognizedWords);
     }
 
     private void init() {
         if (myNeuralNetwork == null) {
             myNeuralNetwork = new NeuralNetworkImpl(new NeuralNetworkStructureImpl());
         }
         if (myLabels == null) {
             myLabels = new Labels(new char[] {
                     'а', 'б', 'в', 'г', 'д', 'Е', 'ж', 'и', 'к', 'л', 'м', 'н', 'п', 'р', 'с',
                     'т', 'у', 'ф', 'х', 'ц', 'ш', 'щ', 'ъ', 'ь', 'э', 'ю', 'я', 'е',
                     '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
             });
         }
     }
 
     private String[] recognizeWords(final double[][] input, final Collection<Rectangle> words) {
         final WordRecognizer wordRecognizer = new WordRecognizer(input, myLabels, myNeuralNetwork);
         final int size = words.size();
         final CountDownLatch latch = new CountDownLatch(size);
         final String[] recognizedWords = new String[size];
         int i = 0;
         for (final Rectangle word : words) {
             final int pos = i++;
             myExecutorService.execute(new Runnable() {
                 @Override
                 public void run() {
                     recognizedWords[pos] = wordRecognizer.recognize(word);
                     latch.countDown();
                     notifyProgress((int) (20 + 80 * ((double) size - latch.getCount()) / size));
                 }
             });
         }
         try {
             latch.await();
         }
         catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
         return recognizedWords;
     }
 
     private String buildResult(final String[] recognizedWords) {
         final StringBuilder builder = new StringBuilder();
         for (final String word : recognizedWords) {
             builder.append(word).append(' ');
         }
         return builder.toString();
     }
 
     // TODO: another thread?
     private void notifyStarted(final String message) {
         for (final ProgressListener progressListener : myProgressListeners) {
             progressListener.started(message);
         }
     }
 
     private void notifyProgress(final int progress) {
         for (final ProgressListener progressListener : myProgressListeners) {
             progressListener.progress(progress);
         }
     }
 
     private void notifyProgress(final int progress, final String message) {
         for (final ProgressListener progressListener : myProgressListeners) {
             progressListener.progress(progress, message);
         }
     }
 
     private void notifyDone(final String message) {
         for (final ProgressListener progressListener : myProgressListeners) {
             progressListener.done(message);
         }
     }
 
     private void setIndeterminate() {
         for (final ProgressListener progressListener : myProgressListeners) {
             progressListener.setIndeterminate(true);
         }
     }
 
     @Override
     public TrainingResult train(final File file) {
         setIndeterminate();
         notifyStarted("Study");
         final TrainingSet trainingSet = buildTrainingSet(file);
         init();
         final TrainingResult trainingResult = myNeuralNetwork.train(trainingSet);
         notifyDone("Study finished. Last error: " + String.valueOf(trainingResult.getLastError()));
         return trainingResult;
     }
 
     // TODO: exceptions
     private TrainingSet buildTrainingSet(final File file) {
         try (final ZipFile zipFile = new ZipFile(file)) {
             readLabels(zipFile);
             return readTrainingSet(zipFile);
         }
         catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void readLabels(final ZipFile zipFile) throws IOException {
         final ZipEntry entry = zipFile.getEntry("labels.csv");
         if (entry == null) {
             throw new RuntimeException("Missing labels.csv file");
         }
         final Reader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
         final List<Character> list = readCSVFile(reader);
         myLabels = new Labels(list);
     }
 
     private TrainingSet readTrainingSet(final ZipFile zipFile) throws IOException {
         final List<TrainingElement> trainingElements = new ArrayList<>();
         final Enumeration<? extends ZipEntry> entries = zipFile.entries();
         while (entries.hasMoreElements()) {
             final ZipEntry entry = entries.nextElement();
             final String name = entry.getName();
             if (!entry.isDirectory() && (name.endsWith("png") || name.endsWith("bmp"))) {
                 System.out.println(name);
                final double[][] data = readImage(zipFile, entry);
                 final String parentFolderName = getParentFolderName(entry);
                 trainingElements.add(new TrainingElementImpl(data, Integer.valueOf(parentFolderName)));
             }
         }
         return new TrainingSetImpl(trainingElements, myLabels.getSize());
     }
 
     private double[][] readImage(final ZipFile zipFile, final ZipEntry entry) throws IOException {
         return ImageReader.readImage(zipFile.getInputStream(entry));
     }
 
     // TODO: better way
     private String getParentFolderName(final ZipEntry entry) {
         final String name = entry.getName();
         final String[] split = name.split("/");
         if (split.length == 1) {
             return "";
         }
         else {
             return split[split.length - 2];
         }
     }
 
     private List<Character> readCSVFile(final Reader reader) throws IOException {
         final List<Character> list = new ArrayList<>();
         int value = reader.read();
         boolean waitComma = false;
         while (value != -1) {
             final char c = (char) value;
             if (!Character.isWhitespace(c)) {
                 if (waitComma) {
                     if (c != ',') {
                         throw new RuntimeException("Invalid csv file");
                     }
                 }
                 else {
                     list.add(c);
                 }
                 waitComma = !waitComma;
             }
             value = reader.read();
         }
         if (!waitComma) {
             throw new RuntimeException("Invalid csv file");
         }
         return list;
     }
 
     @Override
     public void saveRecognizer(final File file) {
         try (final ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file))) {
             stream.writeObject(myLabels);
             stream.writeObject(myNeuralNetwork);
         }
         catch (IOException e) {
             // TODO:
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void loadRecognizer(final File file) {
         try (final ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file))) {
             myLabels = (Labels) stream.readObject();
             myNeuralNetwork = (NeuralNetwork) stream.readObject();
         }
         catch (IOException e) {
             // TODO:
             throw new RuntimeException(e);
         }
         catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
 }
