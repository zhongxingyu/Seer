 /*
  * Created on 22.06.2004
  */
 package org.dotplot.fmatrix;
 
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.dotplot.tokenizer.EOFToken;
 import org.dotplot.tokenizer.EOLToken;
 import org.dotplot.tokenizer.EOSToken;
 import org.dotplot.tokenizer.IFileList;
 import org.dotplot.tokenizer.ITokenStream;
 import org.dotplot.tokenizer.Token;
 import org.dotplot.tokenizer.TokenizerException;
 import org.dotplot.ui.configuration.GlobalConfiguration;
 import org.dotplot.ui.monitor.DotPlotProgressMonitor;
 import org.dotplot.ui.monitor.MonitorablePlotUnit;
 
 /**
  * controller class for the x- and y-Dimension of
  * the fmatrix and the bit matrix.
  * This class represents the "interface"(entry class)
  * to the fmatrix.  All other classes in this package
  * are controlled by this class.
  *
  * @author Constantin von Zitzewitz, Thorsten Ruehl
  * @version	0.4
  */
 public class FMatrixManager implements MonitorablePlotUnit
 {
    private TypeTable typeTable;
    private ITokenStream tokenStream;
    private TokenInformation tokenInformation;
 
    private int progress; // monitor progress
    private String monitorMessage; // monitor message
 
    private IFileList fileList;
    private Vector manualWeightedTokens;
    private Vector storedRegularExpressions;
 
    private final Logger logger = Logger.getLogger(FMatrixManager.class.getName());
 
    /**
     * Default Constructor taking a ITokenStream to construct
     * the fmatrix.
     *
     * @param tokenStream - the token stream(from tokenizer)
     */
    public FMatrixManager(ITokenStream tokenStream)
    {
       this.tokenStream = tokenStream;
       typeTable = new TypeTable(new TokenTable());
       tokenInformation = new TokenInformation();
       progress = 0;
       monitorMessage = null;
       fileList = null;
 
       GlobalConfiguration globalConfig = GlobalConfiguration.getInstance();
       manualWeightedTokens = (Vector) globalConfig.get(GlobalConfiguration.KEY_FMATRIX_TOKEN_WEIGHTS);
       storedRegularExpressions = (Vector) globalConfig.get(GlobalConfiguration.KEY_FMATRIX_REGULAR_EXPRESSIONS);
    }
 
    /**
     * adds the tokens from the stream to the fmatrix.
     *
     * @return boolean - success on adding
     */
    public boolean addTokens()
    {
       if (tokenStream == null)
       {
          return false;
       }
 
       Token token;
       LineInformation lineInformation = new LineInformation();
       File file = null;
 
       int lineNumber = 0;
       int lastLineNumber = 0;
       int firstTokenInLine = 0;
       int lastTokenInLine = 0;
       int tokenIndex = 0;
       int fileIndex = 0; // counts the files
 
       boolean addToken = true;
       boolean processFirstFile = true;
 
       // monitor message
       monitorMessage = "processing Tokens...";
 
       try
       {
 //         token = tokenStream.getNextToken();
 //         file = token.getFile();
 
         while (true))
          {
             token = tokenStream.getNextToken();
             if(token instanceof EOSToken) break;
             
             // ------------ store fileinformation
             if ((token != null) && (processFirstFile || (file != token.getFile())))
             {
                processFirstFile = false;
                file = token.getFile();
                tokenInformation.addFileInformation(new FileInformation(tokenIndex, file));
             }
 
             lineNumber = token.getLine();
 
             if (lastLineNumber == lineNumber)
             {
                lastTokenInLine++;
             }
             else
             {
                lineInformation.addLineInformation(firstTokenInLine, lastTokenInLine, lineNumber);
                firstTokenInLine = tokenIndex;
                //lastTokenInLine  = 0;
             }
 
             lastLineNumber = lineNumber;
 
             // skip linefeeds
             if (token instanceof EOLToken)
             {
                addToken = false;
             }
 
             // take care of the LineInformation object
             if (token instanceof EOFToken)
             {
                tokenInformation.addLineInformationContainer(lineInformation);
                lineInformation = new LineInformation();
                addToken = false;
 
                // count files for progress monitor
                fileIndex++;
                progress = (fileIndex / fileList.count() * 100);
                DotPlotProgressMonitor.getInstance().update();
             }
 
             if (addToken)
             {
                typeTable.addType(token.getValue());
                tokenIndex++; // increment tokenIndex
                lastLineNumber = lineNumber;
             }
 
             addToken = true;
          }
 
          // mark end of fileInformationEntrys and register the FileInformation to the typetable
          tokenInformation.addFileInformation(new FileInformation(tokenIndex, null));
          typeTable.registerTokenInformation(tokenInformation);
       }
       catch (TokenizerException e)
       {
          logger.error("Error adding Tokens", e);
          return false;
       }
 
       typeTable.setAllCalculatedWeight();
       restoreConfiguration();
 
       return true;
    }
 
    /**
     * Loading the userdefined token-weightings from the GlobalConfiguration
     */
    private void restoreConfiguration()
    {
       WeightingEntry weightingEntry;
       TokenType tokenType;
       Iterator iter;
 
       iter = manualWeightedTokens.iterator();
       while (iter.hasNext())
       {
          weightingEntry = (WeightingEntry) iter.next();
 
          tokenType = typeTable.getTokenType(weightingEntry.getTokenIndex());
          tokenType.setWeight(weightingEntry.getWeight());
       }
 
       iter = storedRegularExpressions.iterator();
       while (iter.hasNext())
       {
          typeTable.addRegularExpressionType((String) iter.next(), 0.5);
       }
    }
 
    /**
     * returns an ITypeTableNavigator object.
     *
     * @return ITypeTableNavigator	- the object
     *
     * @see		<code>ITypeTableNavigator</code>
     */
    public ITypeTableNavigator getTypeTableNavigator()
    {
       return typeTable.getNavigator();
    }
 
    /**
     * the function allows to specify a group of types via an regular expression wich should
     * have a special weight thus a group of tokentypes could be highlighted.
     *
     * @param regExp    the regular expression describing the wanted types
     * @param weighting the weight for the new type
     *
     * @return the index of the new regular-expression-type
     */
    public int addRegularExpressionType(String regExp, double weighting)
    {
       return typeTable.addRegularExpressionType(regExp, weighting);
    }
 
    /**
     * @see org.dotplot.ui.monitor.MonitorablePlotUnit#nameOfUnit()
     */
    public String nameOfUnit()
    {
       return "FMatrix Module";
    }
 
    /**
     * @see org.dotplot.ui.monitor.MonitorablePlotUnit#getProgress()
     */
    public int getProgress()
    {
       return progress;
    }
 
    /**
     * @see org.dotplot.ui.monitor.MonitorablePlotUnit#getMonitorMessage()
     */
    public String getMonitorMessage()
    {
       return monitorMessage;
    }
 
    /**
     * Specifies the file list value.
     *
     * @param fileList an IFileList object specifying the file list value
     */
    public void setFileList(IFileList fileList)
    {
       this.fileList = fileList;
    }
 
    /**
     * Returns an Object that hold several functions to manipulate the typetable.
     *
     * @return an ITypeTableManipulator object representing the type table manipulator value
     */
    public ITypeTableManipulator getTypeTableManipulator()
    {
       return new TypeTableManipulator(typeTable);
    }
 
    /**
     * @see org.dotplot.ui.monitor.MonitorablePlotUnit#cancel()
     */
    public void cancel()
    {
    }
 }
