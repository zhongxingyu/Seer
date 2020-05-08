 package designer.deployment;
 
 import java.lang.reflect.Method;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.text.BadLocationException;
 import javax.swing.text.MutableAttributeSet;
 import javax.swing.text.StyledDocument;
 
 import print.PCDiagram;
 import print.PCParagraph;
 import print.PrintEditor;
 import print.ReportDocument;
 import print.ReportPage;
 import statistic.AnalysisTool2;
 import chart.Chart;
 import diagram.PropertiesSetting;
 import diagram.PropertiesTableData;
 import diagram.PropertiesTableReader;
 
 public class DeployObjectFactory {
     private DeployObject deployObject;
     private Hashtable objectTable;
     private Hashtable stringTable;
 
     private Hashtable analysisTable;
 
     private PropertiesTableReader pReader; //
 
     public DeployObjectFactory(DeployObject deployObject) {
         this.deployObject = deployObject;
         objectTable = new Hashtable();
         stringTable = new Hashtable();
         analysisTable = new Hashtable();
 
         pReader = new PropertiesTableReader();
     }
 
     public void showReports(Hashtable analysis, Hashtable charts) {
         int reportCount = deployObject.getReportCount();
         progress = new GenerateProgress();
         for (int i = 0; i < reportCount; i++) {
             ReportDocument reportDocument = deployObject.getReport(i);
             // System.out.println("REPORT PAGES 1!!!!: "+reportDocument.getNumOfReportPage());
             initDocument(reportDocument, analysis, charts);
             progress.setText("Launching report viewer...");
             // System.out.println("REPORT PAGES 2!!!!: "+reportDocument.getNumOfReportPage());
             PrintEditor printViewer = new PrintEditor(reportDocument, true);
             // System.out.println("REPORT PAGES 3!!!!: "+reportDocument.getNumOfReportPage());
             printViewer.setVisible(true);
             printViewer.setLocation(printViewer.getX() + 30 * i, printViewer.getY() + 30 * i);
         }
         progress.setVisible(false);
         progress.dispose();
 
     }
 
     GenerateProgress progress;// = new GenerateProgress();
 
     public void initDocument(ReportDocument reportDocument, Hashtable analysis, Hashtable charts) {
        Enumeration enum = analysis.elements();
         Enumeration keys = analysis.keys();
 
         progress.setVisible(true);
         progress.setText("Generating reports...");
 
         int total = analysis.size();
         total += reportDocument.getNumOfPage();
         int count = 0;
         progress.setMaximun(total);
         progress.setValue(count);
 
        for (; enum.hasMoreElements();) {
             progress.setValue(++count);
 
            AnalysisTool2 at = (AnalysisTool2) enum.nextElement();
             Object key = keys.nextElement();
 
             double[] a = at.getServiceTimes();
             int[] b = at.getThroughPut();
             double[] c = at.getUtilizations();
 
             if (a != null) {
                 a = quickSort(a);
                 analysisTable.put(key + "|" + "Ser", a);
             }
             if (b != null) {
                 b = quickSort(b);
                 analysisTable.put(key + "|" + "Uti", b);
             }
             if (c != null) {
                 c = quickSort(c);
                 analysisTable.put(key + "|" + "Tho", c);
             }
 
         }
 
         int pageCount = reportDocument.getNumOfPage();
         for (int i = 0; i < pageCount; i++) {
 
             ReportPage reportPage = reportDocument.getReportPage(i);
             PCParagraph paragraph[] = reportPage.getParagraph();
             for (int j = 0; j < paragraph.length; j++) {
                 StyledDocument sDocument = paragraph[j].getParagraph();
                 traceValue(sDocument, analysis);
             }
             PCDiagram diagram = reportPage.getDiagram()[0];
             // Object chart =
             // analysis.get(diagram.getObjectComponent().getID());
 
             System.out.println("CHART ID: " + diagram.getObjectComponent().getID());
             if (charts.get(diagram.getObjectComponent().getID()) != null) {
                 // diagram.getObjectComponent().setVisible(true);
                 diagram.setObjectDiagram((Chart) charts.get(diagram.getObjectComponent().getID()));
                 /*
                  * diagram.getObjectComponent().setLayout(new FlowLayout());
                  * diagram.getObjectComponent().setBounds(0,0,500,400);
                  * diagram.getObjectComponent
                  * ().add((Chart)charts.get(diagram.getObjectComponent
                  * ().getID())); diagram.getObjectComponent().updateUI();
                  * diagram.getObjectComponent().repaint();
                  */
 
             }
 
             /*
              * for ( int j = 0 ; j < charts.length ; j ++ ) { if (
              * charts[j].getDataSource().equals(analysisTool) )
              * diagram.setObjectDiagram(charts[j]); }
              */
             diagram.repaint();
             progress.setValue(++count);
         }
     }
 
     public void traceValue(StyledDocument sDocument, Hashtable anlysisTable) {
         String testString;
         try {
             testString = sDocument.getText(0, sDocument.getLength());
 
             System.out.println("MATCHING... " + testString);
             Pattern pattern = Pattern.compile("\\[%\\$(.)*\\$%\\]");
             Matcher matcher = null;
             boolean valid = false;
             matcher = pattern.matcher(testString);
             // valid = matcher.find();
 
             int count = matcher.groupCount();
             System.out.println("MATCH COUNT: " + matcher.groupCount());
 
             while (matcher.find()) {
                 // for (int i = 0; i < count; i++)
                 // {
 
                 String match = matcher.group();
                 System.out.println("--> MATCH: " + match);
                 int lastIndex = match.lastIndexOf("|");
                 String name = match.substring(3, lastIndex);
                 String setMode = match.substring(lastIndex + 1, match.length() - 3);
                 System.out.println("NAME: " + name);
                 System.out.println("SET MODE: " + setMode);
 
                 String replaceString = null;
                 // try
                 // {
                 // replaceString = stringTable.get(match.substring(3,
                 // match.length() - 3)).toString();
                 Object object;
                 object = deployObject.getInputObject(name, setMode);
                 if (object != null) {
                     replaceString = ((InputComponent) object).getDiaplayValue();
                 } else {
                     object = deployObject.getInternalInputObject(name, setMode);
 
                     if (object != null) {
                         replaceString = ((InputComponent) object).getDiaplayValue();
                     } else {
                         Object ao = analysisTable.get(name);
                         if (ao == null) {
                             replaceString = "N/A";
                         } else if (ao instanceof int[]) {
                             int[] io = (int[]) ao;
                             if (setMode.equalsIgnoreCase("Ave")) {
                                 replaceString = Double.toString(getAvg(io));
                             } else if (setMode.equalsIgnoreCase("Mea")) {
                                 replaceString = Integer.toString(getMean(io));
                             } else if (setMode.equalsIgnoreCase("Max")) {
                                 replaceString = Integer.toString(getMax(io));
                             } else if (setMode.equalsIgnoreCase("Min")) {
                                 replaceString = Integer.toString(getMin(io));
                             }
                         } else if (ao instanceof double[]) {
                             double[] doo = (double[]) ao;
                             if (setMode.equalsIgnoreCase("Ave")) {
                                 replaceString = Double.toString(getAvg(doo));
                             } else if (setMode.equalsIgnoreCase("Mea")) {
                                 replaceString = Double.toString(getMean(doo));
                             } else if (setMode.equalsIgnoreCase("Max")) {
                                 replaceString = Double.toString(getMax(doo));
                             } else if (setMode.equalsIgnoreCase("Min")) {
                                 replaceString = Double.toString(getMin(doo));
                             }
                         }
                     }
                 }
                 // }
                 // catch ( Exception ex )
                 // {
 
                 // }
 
                 System.out.println("REPLACE STRING: " + replaceString);
                 int start = testString.indexOf(match);
                 int end = start + match.length();
                 /*
                  * MutableAttributeSet attr = new SimpleAttributeSet();
                  * StyleConstants.setBold (attr, b);
                  * jTextPane.setCharacterAttributes(attr, false);
                  */
                 MutableAttributeSet attr = sDocument.getLogicalStyle(start);
                 sDocument.remove(start, match.length());
                 sDocument.insertString(start, replaceString, attr);
 
                 testString = sDocument.getText(0, sDocument.getLength());
                 matcher = pattern.matcher(testString);
             }
             // }
         } catch (BadLocationException ex) {
             System.out.println(ex);
             return;
         }
 
     }
 
     public Hashtable initTable() throws InvalidDataException {
         objectTable.clear();
         stringTable.clear();
 
         int numberOfInputObject = deployObject.getInputObjectCount();
         InputComponent input;
 
         System.out.println("================================================\n");
 
         for (int i = 0; i < numberOfInputObject; i++) {
             InputComponent currentInputComponent = deployObject.getInputObject(i);
             System.out.println("==========" + currentInputComponent.getName() + "==========");
             try {
                 String variableName = currentInputComponent.getName();
                 String setMode = currentInputComponent.getSetMode();
                 String name = variableName + "|" + setMode;
                 System.out.println("Get Name: " + name);
                 /*
                  * System.out.println( variableName + " / " +
                  * currentInputComponent.getInput() ); System.out.println(
                  * setMode + " / " + currentInputComponent.getInput());
                  */
 
                 if (setMode.equalsIgnoreCase("Object Creator") || setMode.equalsIgnoreCase("Distribution") || setMode.equalsIgnoreCase("Transform") || setMode.equalsIgnoreCase("SplitterModel") || setMode.equalsIgnoreCase("MergerModel")) {
                     String className = currentInputComponent.getInput().toString();
                     Object object = (Class.forName("mcomponent.distribution." + className)).newInstance();
 
                     System.out.println("Value: " + className);
                     /*
                      * int dotPosition =
                      * object.getClass().getName().lastIndexOf("."); String
                      * className =
                      * object.getClass().getName().substring(dotPosition+1);
                      */
                     PropertiesTableData[] pDataAry = pReader.getPropertiesTableData(object.getClass().getName());
                     PropertiesSetting pSetting = new PropertiesSetting();
 
                     String getMethod = "";
                     String arg = "";
 
                     for (int j = 0; j < pDataAry.length; j++) {
                         getMethod = pDataAry[j].getGetMethod();
                         System.err.println("getMethod:" + getMethod);
                         if (getMethod != null && !getMethod.equals("null")) {
                             String setMethodName = pDataAry[j].getSetMethod();
 
                             String interInputComponentToFind = variableName + "|" + className;
                             // System.out.println("["+i+"]"+">>>>3: "+interInputComponentToFind
                             // + ", " + pDataAry[j].getName());
                             Object getInput[] = new Object[1];
                             Class[] classPara = new Class[1];
 
                             // System.out.println(deployObject.getInternalInputObject(interInputComponentToFind,
                             // pDataAry[j].getName()).getInput());
 
                             getInput[0] = deployObject.getInternalInputObject(interInputComponentToFind, pDataAry[j].getName()).getInput();
                             classPara[0] = getInput[0].getClass();
 
                             Method method = object.getClass().getDeclaredMethod(setMethodName, classPara);
                             method.invoke(object, getInput);
 
                             System.out.println("  Internal value: " + setMethodName + "( " + getInput[0] + " )");
 
                         }
                     }
 
                     objectTable.put(name, object);
                     stringTable.put(name, currentInputComponent.getDiaplayValue());
                     System.out.println(">>>Object created>>>: " + object);
                 } else {
                     Object object = currentInputComponent.getInput();
                     objectTable.put(name, object);
                     System.out.println("Value: " + object);
                 }
             } catch (java.lang.ClassNotFoundException e) {
                 System.out.println(e);
             } catch (java.lang.NoSuchMethodException e) {
                 System.out.println(e);
             } catch (java.lang.IllegalAccessException e) {
                 System.out.println(e);
             } catch (java.lang.InstantiationException e) {
                 System.out.println(e);
             } catch (java.lang.reflect.InvocationTargetException e) {
                 System.out.println(e);
             }
         }
         Enumeration enum1 = objectTable.elements();
         Enumeration enum2 = objectTable.keys();
         for (; enum1.hasMoreElements();) {
             String key = enum2.nextElement().toString();
             String sub = "                                        ";
             key = key.concat(sub.substring(key.length()));
             System.out.println(key + "\t:   " + enum1.nextElement());
         }
         return objectTable;
     }
 
     public int[] quickSort(int[] in) {
         int[] newin = new int[in.length];
 
         for (int i = 0; i < in.length; i++) {
             newin[i] = in[i];
         }
 
         qsort(0, newin.length - 1, in, newin);
 
         return newin;
     }
 
     private void qsort(int lo, int hi, int[] in, int newin[]) {
         int i, j, compareResult;
 
         int temp, guessed_median;
 
         i = lo;
         j = hi;
 
         guessed_median = newin[((lo + hi) / 2)];
 
         while (i < j) {
 
             compareResult = newin[i] - guessed_median;
 
             while (compareResult < 0) {
                 i++;
                 compareResult = newin[i] - guessed_median;
             }
 
             compareResult = newin[j] - guessed_median;
 
             while (compareResult > 0) {
                 j--;
                 compareResult = newin[j] - guessed_median;
             }
 
             if (i <= j) {
                 temp = newin[i];
                 newin[i] = newin[j];
                 newin[j] = temp;
 
                 i++;
                 j--;
             }
         }
 
         if (lo < j) {
             qsort(lo, j, in, newin);
         }
         if (i < hi) {
             qsort(i, hi, in, newin);
         }
     }
 
     public int getMax(int[] i) {
         return i[i.length - 1];
     }
 
     public int getMin(int[] i) {
         return i[0];
     }
 
     public int getMean(int[] i) {
         return i[(i.length / 2)];
     }
 
     public double getAvg(int[] i) {
         int sum = 0;
         for (int j = 0; j < i.length; j++) {
             sum += i[j];
         }
         return sum / i.length;
     }
 
     public double getMax(double[] i) {
         return i[i.length - 1];
     }
 
     public double getMin(double[] i) {
         return i[0];
     }
 
     public double getMean(double[] i) {
         return i[(i.length / 2)];
     }
 
     public double getAvg(double[] i) {
         double sum = 0;
         for (int j = 0; j < i.length; j++) {
             sum += i[j];
         }
         return sum / i.length;
     }
 
     public double[] quickSort(double[] in) {
         double[] newin = new double[in.length];
 
         for (int i = 0; i < in.length; i++) {
             newin[i] = in[i];
         }
 
         qsort(0, newin.length - 1, in, newin);
 
         return newin;
     }
 
     private void qsort(int lo, int hi, double[] in, double newin[]) {
         int i, j;
         double compareResult;
         double temp;
         double guessed_median;
         i = lo;
         j = hi;
 
         guessed_median = newin[((lo + hi) / 2)];
 
         while (i < j) {
 
             compareResult = newin[i] - guessed_median;
 
             while (compareResult < 0) {
                 i++;
                 compareResult = newin[i] - guessed_median;
             }
 
             compareResult = newin[j] - guessed_median;
 
             while (compareResult > 0) {
                 j--;
                 compareResult = newin[j] - guessed_median;
             }
 
             if (i <= j) {
                 temp = newin[i];
                 newin[i] = newin[j];
                 newin[j] = temp;
 
                 i++;
                 j--;
             }
         }
 
         if (lo < j) {
             qsort(lo, j, in, newin);
         }
         if (i < hi) {
             qsort(i, hi, in, newin);
         }
     }
 }
