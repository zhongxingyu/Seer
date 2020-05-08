 package org.geworkbench.components.parsers.microarray;
 
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.components.parsers.FileFormat;
 import org.geworkbench.components.parsers.InputFileFormatException;
 
 import java.io.File;
 
 /**
  * <p>Title: Sequence and Pattern Plugin</p>
  * <p>Description: </p>
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: </p>
  *
  * @author not attributable
  * @version 1.0
  */
 
 
 public abstract class DataSetFileFormat extends FileFormat {
   public DataSetFileFormat() {
   }
   abstract public DSDataSet getDataFile(File file) throws InputFileFormatException;
   public DSDataSet getDataFile(File file, String compatibilityLabel) throws InputFileFormatException, UnsupportedOperationException {
       throw new UnsupportedOperationException();
   }
   abstract public DSDataSet getDataFile(File[] files) throws InputFileFormatException;
  
  /**
   * The reason that this field progressMessage needs to be added here:
   * Progress message is updated by ProjectPanel 
   * and used by ProgressMonitorInputStream through DataSetFileFormat interface in ProjectPanel's
   * fileOpenAction(final File[] dataSetFiles,	final org.geworkbench.components.parsers.FileFormat inputFormat, boolean merge) .
   */
  private String progressMessage;
  public String getProgressMessage() {return progressMessage; }
  public void setProgressMessage(String p) { progressMessage=p; }
 }
