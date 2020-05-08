 package com.dthielke.nnet.backprop;
 
 import java.awt.GridLayout;
 import java.io.FileNotFoundException;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JFrame;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 import com.dthielke.nnet.io.DataLoader;
 
 public class Driver {
 
     private static BackPropNetwork network;
     private static BackPropNetworkPanel networkPanel;
 
    private static int errorResolution = 10;
     private static List<Double> errorBuffer = new LinkedList<Double>();
     private static XYSeries errors = new XYSeries("RMS Error");
 
     public static void main(String[] args) throws InterruptedException, FileNotFoundException {
         DataLoader loader = new DataLoader();
         loader.load("data/wine.data");
         double[][] inputs = loader.getNormalizedInputs();
         double[][] targets = loader.getTargets();

        int[] structure = new int[] { inputs[0].length, 6, targets[0].length };
         network = new BackPropNetwork(structure, 1.0, 0.4, 0.6);
         createGUI();
 
        double error = 0;
        int errorCount = 0;
         while (true) {
             int dataset = (int) (Math.random() * inputs.length);
             synchronized (network) {
                 network.train(inputs[dataset], targets[dataset], 0.25, 0.75, 0.0);
             }
            if (errorCount == errorResolution) {
                synchronized (errorBuffer) {
                    errorBuffer.add(Math.sqrt(error / errorCount));
                }
                error = 0;
                errorCount = 0;
             }
            error += Math.pow(network.getError(), 2);
            errorCount++;
             Thread.sleep(1);
         }
     }
 
     public static void createGUI() {
         networkPanel = new BackPropNetworkPanel(network);
 
         XYSeriesCollection data = new XYSeriesCollection(errors);
         JFreeChart chart = ChartFactory.createXYLineChart("", "Epoch", "Error", data, PlotOrientation.VERTICAL, false, false, false);
         ChartPanel chartPanel = new ChartPanel(chart);
         chart.setAntiAlias(true);
         chart.setTextAntiAlias(true);
 
         JFrame frame = new JFrame("Backpropagation Neural Network");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.getContentPane().setLayout(new GridLayout(1, 2));
         frame.getContentPane().add(networkPanel);
         frame.getContentPane().add(chartPanel);
         frame.setSize(800, 600);
         frame.pack();
         frame.setVisible(true);
 
         new Thread(new VisualizationUpdater()).start();
         new Thread(new GraphUpdater()).start();
     }
 
     static class VisualizationUpdater implements Runnable {
 
         @Override
         public void run() {
             while (true) {
                 synchronized (network) {
                     networkPanel.repaint();
                 }
                 try {
                     Thread.sleep(20);
                 } catch (InterruptedException ex) {
                     System.out.println(ex);
                 }
             }
         }
 
     }
 
     static class GraphUpdater implements Runnable {
 
         private int epoch = 0;
 
         @Override
         public void run() {
             while (true) {
                 synchronized (errorBuffer) {
                     errors.setNotify(false);
                     for (double error : errorBuffer) {
                        epoch += errorResolution;
                        errors.add(epoch, error);
                     }
                     errorBuffer.clear();
                     errors.setNotify(true);
                 }
                 try {
                     Thread.sleep(250);
                 } catch (InterruptedException ex) {
                     System.out.println(ex);
                 }
             }
         }
 
     }
 
 }
