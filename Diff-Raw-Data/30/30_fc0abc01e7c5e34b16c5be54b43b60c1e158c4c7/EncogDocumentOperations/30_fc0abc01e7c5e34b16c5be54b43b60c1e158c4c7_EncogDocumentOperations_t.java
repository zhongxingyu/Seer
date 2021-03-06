 /*
  * Encog(tm) Workbanch v3.1 - Java Version
  * http://www.heatonresearch.com/encog/
  * http://code.google.com/p/encog-java/
  
  * Copyright 2008-2012 Heaton Research, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *   
  * For more information on Heaton Research copyrights, licenses 
  * and trademarks visit:
  * http://www.heatonresearch.com/copyright
  */
 package org.encog.workbench.frames.document;
 
 import java.awt.Frame;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 
 import org.encog.Encog;
 import org.encog.mathutil.error.ErrorCalculation;
 import org.encog.mathutil.error.ErrorCalculationMode;
 import org.encog.mathutil.randomize.RangeRandomizer;
 import org.encog.ml.MLError;
 import org.encog.ml.MLMethod;
 import org.encog.ml.bayesian.BayesianNetwork;
 import org.encog.ml.bayesian.bif.BIFUtil;
 import org.encog.ml.data.MLDataPair;
 import org.encog.ml.data.MLDataSet;
 import org.encog.ml.data.buffer.BufferedMLDataSet;
 import org.encog.util.Format;
 import org.encog.util.file.Directory;
 import org.encog.workbench.EncogWorkBench;
 import org.encog.workbench.config.EncogWorkBenchConfig;
 import org.encog.workbench.dialogs.BenchmarkDialog;
 import org.encog.workbench.dialogs.EvaluateDialog;
 import org.encog.workbench.dialogs.binary.DialogNoise;
 import org.encog.workbench.dialogs.config.EncogConfigDialog;
 import org.encog.workbench.dialogs.newdoc.CreateNewDocument;
 import org.encog.workbench.dialogs.select.SelectDialog;
 import org.encog.workbench.dialogs.select.SelectItem;
 import org.encog.workbench.dialogs.training.ProbenDialog;
 import org.encog.workbench.dialogs.trainingdata.CreateTrainingDataDialog;
 import org.encog.workbench.dialogs.trainingdata.TrainingDataType;
 import org.encog.workbench.frames.EncogCommonFrame;
 import org.encog.workbench.frames.document.tree.ProjectFile;
 import org.encog.workbench.frames.document.tree.ProjectItem;
 import org.encog.workbench.process.CreateTrainingData;
 import org.encog.workbench.tabs.BrowserFrame;
 import org.encog.workbench.tabs.EncogCommonTab;
 import org.encog.workbench.tabs.files.text.BasicTextTab;
 import org.encog.workbench.tabs.proben.ProbenStatusTab;
 import org.encog.workbench.tabs.rbf.RadialBasisFunctionsTab;
 import org.encog.workbench.util.FileUtil;
 
 public class EncogDocumentOperations {
 
 	private EncogDocumentFrame owner;
 
 	public EncogDocumentOperations(EncogDocumentFrame owner) {
 		this.owner = owner;
 	}
 	
 	private void displayBugWarning() {
 		EncogWorkBench
 		.displayError(
 				"Can't Delete/Modify",
 				"Unfortunatly, due to a limitation in Java, EGB files cannot be deleted/changed once opened.\nRestart the workbench, and you will be able to delete this file.");
 
 	}
 
 	public void performEditCopy() {
 		final Frame frame = EncogWorkBench.getCurrentFocus();
 		if (frame instanceof EncogCommonFrame) {
 			final EncogCommonFrame ecf = (EncogCommonFrame) frame;
 			ecf.copy();
 		}
 
 	}
 
 	public void performEditCut() {
 		final Frame frame = EncogWorkBench.getCurrentFocus();
 		if (frame instanceof EncogCommonFrame) {
 			final EncogCommonFrame ecf = (EncogCommonFrame) frame;
 			ecf.cut();
 		}
 	}
 
 	public void performEditPaste() {
 		final Frame frame = EncogWorkBench.getCurrentFocus();
 		if (frame instanceof EncogCommonFrame) {
 			final EncogCommonFrame ecf = (EncogCommonFrame) frame;
 			ecf.paste();
 		}
 
 	}
 
 	public void performFileNewProject() {
 
 		CreateNewDocument dialog = new CreateNewDocument(EncogWorkBench
 				.getInstance().getMainWindow());
 		dialog.getParentDirectory().setValue(
 				EncogWorkBench.getInstance().getEncogFolders().toString());
 		dialog.getProjectFilename().setValue("MyEncogProject");
 
 		if (dialog.process()) {
 			File parent = new File(dialog.getParentDirectory().getValue());
 			File project = new File(parent, dialog.getProjectFilename()
 					.getValue());
 			Directory.deleteDirectory(project); // the user was warned!
 			project.mkdir();
 
 			EncogWorkBench.getInstance().getMainWindow().getTree()
 					.refresh(project);
 
 		}
 	}
 
 	public void performFileChooseDirectory() {
 		try {
 			final JFileChooser fc = new JFileChooser();
 			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			fc.setCurrentDirectory(EncogWorkBench.getInstance()
 					.getEncogFolders());
 			final int result = fc.showOpenDialog(owner);
 			if (result == JFileChooser.APPROVE_OPTION) {
 				File path = fc.getSelectedFile().getAbsoluteFile();
 				EncogWorkBench.getInstance().getMainWindow()
 						.changeDirectory(path);
 			}
 		} catch (final Throwable e) {
 			EncogWorkBench.displayError("Can't Change Directory", e);
 			e.printStackTrace();
 			EncogWorkBench.getInstance().getMainWindow().endWait();
 		}
 	}
 
 	public void performBrowse() {
 		BrowserFrame browse = new BrowserFrame();
 		this.owner.getTabManager().openTab(browse);
 	}
 
 	public void performRBF() {
 		RadialBasisFunctionsTab rbf = new RadialBasisFunctionsTab();
 		this.owner.getTabManager().openTab(rbf);
 	}
 
 	public void performHelpAbout() {
 		EncogWorkBench.getInstance().getMainWindow().displayAboutTab();
 	}
 
 	public void performEditConfig() {
 
 		EncogConfigDialog dialog = new EncogConfigDialog(EncogWorkBench
 				.getInstance().getMainWindow());
 
 		EncogWorkBenchConfig config = EncogWorkBench.getInstance().getConfig();
 
 		dialog.getDefaultError().setValue(config.getDefaultError());
 		dialog.getThreadCount().setValue(config.getThreadCount());
 		dialog.getTrainingChartHistory().setValue(config.getTrainingHistory());
 		dialog.getDisplayTrainingImprovement().setValue(config.isShowTrainingImprovement());
 		dialog.getIterationStepCount().setValue(config.getIterationStepCount());
 		//dialog.getUseOpenCL().setValue(config.isUseOpenCL());
 		switch (config.getErrorCalculation()) {
 		case RMS:
 			((JComboBox) dialog.getErrorCalculation().getField())
 					.setSelectedIndex(0);
 			break;
 		case MSE:
 			((JComboBox) dialog.getErrorCalculation().getField())
 					.setSelectedIndex(1);
 			break;
 		}
 
 		if (dialog.process()) {
 			config.setDefaultError(dialog.getDefaultError().getValue());
 			config.setThreadCount(dialog.getThreadCount().getValue());
 			//config.setUseOpenCL(dialog.getUseOpenCL().getValue());
 			switch (((JComboBox) dialog.getErrorCalculation().getField())
 					.getSelectedIndex()) {
 			case 0:
 				config.setErrorCalculation(ErrorCalculationMode.RMS);
 				break;
 			case 1:
 				config.setErrorCalculation(ErrorCalculationMode.MSE);
 				break;
 			}
 			EncogWorkBench.getInstance().getConfig().saveConfig();
 
 			ErrorCalculation.setMode(EncogWorkBench.getInstance().getConfig()
 					.getErrorCalculation());
 			
 			config.setTrainingHistory(dialog.getTrainingChartHistory().getValue());
 			config.setShowTrainingImprovement(dialog.getDisplayTrainingImprovement().getValue());
 			config.setIterationStepCount(dialog.getIterationStepCount().getValue());
 
 			/*if (config.isUseOpenCL() && Encog.getInstance().getCL() == null) {
 				EncogWorkBench.initCL();
 				if (Encog.getInstance().getCL() != null) {
 					EncogWorkBench
 							.displayMessage("OpenCL",
 									"Success, your graphics card(s) are now ready to help train neural networks.");
 				}
 			} else if (!EncogWorkBench.getInstance().getConfig().isUseOpenCL()
 					&& Encog.getInstance().getCL() != null) {
 				EncogWorkBench
 						.displayMessage(
 								"OpenCL",
 								"Encog Workbench will stop using your GPU the next time\nthe workbench is restarted.");
 			}*/
 		}
 	}
 
 	public void performEvaluate() {
 		try {
 			EvaluateDialog dialog = new EvaluateDialog();
 			if (dialog.process()) {
 				MLMethod method = dialog.getNetwork();
 				MLDataSet training = dialog.getTrainingSet();
 
 				double error = 0;
 
 				if (method instanceof MLError) {
 					error = ((MLError) method).calculateError(training);
 					EncogWorkBench.displayMessage("Error For this Network", ""
 							+ Format.formatPercent(error));
 
 				} else {
 					EncogWorkBench.displayError("Error",
 							"The Machine Learning method "
 									+ method.getClass().getSimpleName()
 									+ " does not support error calculation.");
 				}
 			}
 		} catch (Throwable t) {
 			EncogWorkBench.displayError("Error Evaluating Network", t);
 		}
 
 	}
 
 	public void performBenchmark() {
 		if (EncogWorkBench
 				.askQuestion(
 						"Benchmark",
 						"Would you like to benchmark Encog on this machine?\nThis process will take several minutes to complete.")) {
 			BenchmarkDialog dialog = new BenchmarkDialog();
 			dialog.setVisible(true);
 		}
 
 	}
 
 	public void performCreateTrainingData() throws IOException {
 		CreateTrainingDataDialog dialog = new CreateTrainingDataDialog(
 				EncogWorkBench.getInstance().getMainWindow());
 
 		dialog.setTheType(TrainingDataType.CopyCSV);
 
 		if (dialog.process()) {
 			String name = dialog.getFilenameName();
 
 			if (name.trim().length() == 0) {
 				EncogWorkBench
 						.displayError("Error", "Must specify a filename.");
 				return;
 			}
 
 			name = FileUtil.forceExtension(name, "csv");
 			File targetFile = new File(EncogWorkBench.getInstance()
 					.getProjectDirectory(), name);
 
 			if (!EncogWorkBench.getInstance().getMainWindow().getTabManager()
 					.queryViews(targetFile)) {
 				return;
 			}
 
 			switch (dialog.getTheType()) {
 			case CopyCSV:
 				CreateTrainingData.copyCSV(name);
 				break;
 			case MarketWindow:
 				CreateTrainingData.downloadMarketData(name);
 				break;
 			case Random:
 				CreateTrainingData.generateRandom(name);
 				break;
 			case XORTemp:
 				CreateTrainingData.generateXORTemp(name);
 				break;
 			case XOR:
 				CreateTrainingData.copyXOR(name);
 				break;
 			case Iris:
 				CreateTrainingData.copyIris(name);
 				break;
 			case Sunspots:
 				CreateTrainingData.downloadSunspots(name);
 				break;
 			case Digits:
 				CreateTrainingData.copyDigits(name);
 				break;
 			case Patterns1:
 				CreateTrainingData.copyPatterns1(name);
 				break;
 			case Patterns2:
 				CreateTrainingData.copyPatterns2(name);
 				break;
 			case Download:
 				CreateTrainingData.downloadURL(name);
 				break;
 			case Encoder:
 				CreateTrainingData.generateEncoder(name);
 				break;
 			case Linear:
 				CreateTrainingData.generateLinear(name);
 				break;
 			case SineWave:
 				CreateTrainingData.generateSineWave(name);
 				break;
 			}
 			EncogWorkBench.getInstance().refresh();
 		}
 	}
 
 	public void performQuit() {
 		EncogWorkBench.getInstance().getMainWindow().getTabManager().closeAll();
 		System.exit(0);
 	}
 
 	public void performFileProperties(ProjectFile selected) {
 		String name = selected.getFile().getName();
 		String newName = EncogWorkBench
 				.displayInput("What would you like to rename the file \""
 						+ name + "\" to?");
 		if (newName != null) {
 			File oldFile = selected.getFile();
 			File dir = oldFile.getParentFile();
 			File newFile = new File(dir, newName);
 			if( !oldFile.renameTo(newFile) ) {
 				if( oldFile.getName().toLowerCase().endsWith(".egb")) {
 					displayBugWarning();
 				} else {
 					EncogWorkBench.displayError("Error", "Rename failed.");
 				}
 			}
 			EncogWorkBench.getInstance().refresh();
 		}
 
 	}
 
 	public void performSave() {
 		EncogCommonTab tab = this.owner.getTabManager().getCurrentTab();
 		if (tab != null) {
 			tab.save();
 		}
 
 	}
 
 	public void performDelete() {
 
 		boolean first = true;
 		List<ProjectItem> list = this.owner.getTree().getSelectedValue();
 
 		for (ProjectItem selected : list) {
 			if (first
 					&& !EncogWorkBench.askQuestion("Warning",
 							"Are you sure you want to delete these file(s)?")) {
 				return;
 			}
 			first = false;
 			if (selected instanceof ProjectFile) {
 				File f = ((ProjectFile) selected).getFile();
				
				EncogWorkBench.getInstance().getMainWindow().getTabManager().closeAll(f);
				
 				if (!f.delete()) {
 					if (FileUtil.getFileExt(f).equalsIgnoreCase("egb")) {
 						displayBugWarning();
 					} else {
 						EncogWorkBench.displayError("Can't Delete",
 								f.toString());
 					}
 
				} 
 			}
 			EncogWorkBench.getInstance().getMainWindow().getTree().refresh();
 		}
 	}
 
 	public void performEditFind() {
 		EncogCommonTab tab = EncogWorkBench.getInstance().getMainWindow()
 				.getTabManager().getCurrentTab();
 
 		if (tab instanceof BasicTextTab) {
 			((BasicTextTab) tab).find();
 		}
 
 	}
 
 	public void importFile() {
 		SelectItem selectBIF;
 
 		List<SelectItem> list = new ArrayList<SelectItem>();
 		list.add(selectBIF = new SelectItem("Bayesian Network",
 				"Bayesian Network contained in XML-BIF."));
 
 		SelectDialog sel = new SelectDialog(EncogWorkBench.getInstance()
 				.getMainWindow(), list);
 		sel.setVisible(true);
 
 		if (sel.getSelected() == selectBIF) {
 			importBIF();
 		}
 	}
 
 	public void importBIF() {
 		try {
 			final JFileChooser fc = new JFileChooser();
 			fc.setCurrentDirectory(EncogWorkBench.getInstance()
 					.getEncogFolders());
 			final int result = fc.showOpenDialog(owner);
 			if (result == JFileChooser.APPROVE_OPTION) {
 				BayesianNetwork net = BIFUtil.readBIF(fc.getSelectedFile());
 				File path = new File(EncogWorkBench.getInstance()
 						.getProjectDirectory(), FileUtil.forceExtension(fc
 						.getSelectedFile().getName(), "eg"));
 				EncogWorkBench.getInstance().save(path, net);
 				EncogWorkBench.getInstance().refresh();
 			}
 		} catch (final Throwable e) {
 			EncogWorkBench.displayError("Can't Change Directory", e);
 			e.printStackTrace();
 			EncogWorkBench.getInstance().getMainWindow().endWait();
 		}
 	}
 
 	public void performProben() {
 		ProbenDialog dialog = new ProbenDialog();
 		if( dialog.process() ) {
 			ProbenStatusTab tab = new ProbenStatusTab(
 					dialog.getTrainingRuns(),
 					dialog.getMaxIterations(),
 					dialog.getMethodName(), 
 					dialog.getMethodArchitecture(), 
 					dialog.getTrainingName(), 
 					dialog.getTrainingArgs());
 			EncogWorkBench.getInstance().getMainWindow().getTabManager().openTab(tab);
 		}
 		
 	}
 
 	public void performNoise() {
 		DialogNoise dialog = new DialogNoise();
 		
 		if( dialog.process() ) {
 			File sourceFile = dialog.getSourceFile();
 			File targetFile = new File(sourceFile.getParent(),dialog.getTargetFile().getValue());
 			
 			double inputPercent = dialog.getInputNoise().getValue();
 			double idealPercent = dialog.getIdealNoise().getValue();
 			
 			BufferedMLDataSet sourceData = new BufferedMLDataSet(sourceFile);
 			BufferedMLDataSet targetData = new BufferedMLDataSet(targetFile);
 			targetData.beginLoad(sourceData.getInputSize(), sourceData.getIdealSize());
 			for(MLDataPair pair : sourceData) {
 				// add noise to the input
 				if (inputPercent > Encog.DEFAULT_DOUBLE_EQUAL) {
 					for (int i = 0; i < pair.getInput().size(); i++) {
 						double d = pair.getInput().getData(i);
 						double r = d * inputPercent;
 						d += RangeRandomizer.randomize(-r, r);
 						pair.getInput().setData(i, d);
 					}
 				}
 				// add noise to the ideal
 				if (idealPercent > Encog.DEFAULT_DOUBLE_EQUAL) {
 					for (int i = 0; i < pair.getIdeal().size(); i++) {
 						double d = pair.getIdeal().getData(i);
 						double r = d * idealPercent;
 						d += RangeRandomizer.randomize(-r, r);
 						pair.getIdeal().setData(i, d);
 					}
 				}
 				
 				// write it
 				targetData.add(pair);
 			}
 			sourceData.close();
 			targetData.close();
 			EncogWorkBench.getInstance().getMainWindow().getTree().refresh();
 		}
 		
 	}
 }
