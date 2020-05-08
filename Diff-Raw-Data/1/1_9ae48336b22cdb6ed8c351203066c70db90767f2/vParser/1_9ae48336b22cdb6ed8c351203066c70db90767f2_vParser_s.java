 package TestMain;
 
 /**
  * @author Kenneth Hassey
  * @date: 1/15/2013
  * @Version 1.001
  *
  * Function:
  *      This is the specific parser for the Verilog file type.  This parser will
  *      setup all lines inside the code for the Verilog error searcher.
  * Status: Untested.
  */
 
 
 import java.util.ArrayList;
 
 public class vParser {
     private FileStore fs;
     private Code c;
     private Variable v;
     private String FileType;
     private Error e;
     private ErrorDatabase db;
     private ArrayList<String> UnparsedCode;
     private ArrayList<Code> CodeList;
 
     vParser()
     {
         fs = new FileStore();
         c = new Code();
         v = new Variable();
         FileType = "";
         e = new Error();
         db = new ErrorDatabase();
     }
     vParser(String fileType,ArrayList<String> unp)
     {
         this();
         FileType = fileType;
     }
     vParser(ArrayList<String> unp, String fileType)
     {
         this(fileType, unp);
     }
 
     //returns the finished file
     public FileStore getFileStore()
     {
         return fs;
     }
     public ErrorDatabase getErrorDatabase()
     {
         return db;
     }
 
     //allows setting of the file type
     public void setFileType(String ft)
     {
         FileType = ft;
     }
 
     //Returns the altered database for the parsed errors.
     public void setErrorDatabase(ErrorDatabase d)
     {
         db = d;
     }
 
     //this contains the list of instructions that the parser must do in a
     //in a certain order.
     public void start()
     {
         fs = new FileStore();
         vParserStage1();
     }
 
 
 
     //Stage one is responsible for the seperation of lines that have multiple
     //instructions to them.  This seperates all code into one instruction per
     //line as a per parsing so that the search functions have one line per
     //instuction.
     private void vParserStage1()
     {
         //Hard Coded Error Message for parser specific error message
         e = new Error();
         e.setErrorNum("HC01V");
         e.setErrorMsg("Parser Reporting multiple instruction per line. Parsed"
                 + "Instructions will be changed to multiple lines");
         db.addError(e);
 
 
         String currentLine;
         String StartingLine;
         String newChar;
         int added;
         CodeList = new ArrayList();
 
         //read each line of the unparsed code
         for(int i = 0; i <UnparsedCode.size();i++)
         {
             currentLine = UnparsedCode.get(i);
             StartingLine = currentLine;
             newChar = " ";
             int ignoresc = 0;
 
             int index = 0;
             while(!(currentLine.equals("")))
             {
                 c = new Code();
                 added = 0;
                  newChar = ""+currentLine.charAt(index);
 
                  //for loop checking.
                  if(newChar.equals(";"))
                  {
                      StartingLine = StartingLine+newChar;
                      //if the check does show a for loop
                      if(StartingLine.contains(" for(")||StartingLine.contains(" for "))
                      {
                          index++;
 
                          ignoresc = 1;
                          while(ignoresc != 0)
                          {
                              newChar = ""+currentLine.charAt(index);
                              //ignores the second one
                              if(newChar.equals(";"))
                              {
                                  ignoresc--;
                              }
                              StartingLine = StartingLine+newChar;
                              index++;
                          }
                      }
                      else
                      {
                          currentLine= currentLine.substring(index);
                          c.setOriginal(StartingLine);
                          c.setLineNumber(i);
                          CodeList.add(c);
                          StartingLine = "";
                          index = 0;
                          added = 1;
                      }
                  }
 
                  StartingLine = StartingLine+newChar;
 
                  //begin Statement Checking.
                  if(StartingLine.contains(" begin ")||StartingLine.contains(" begin\n"))
                  {
                      currentLine = currentLine.substring(index);
                      c.setOriginal(StartingLine);
                      c.setLineNumber(i);
                      CodeList.add(c);
                      StartingLine = "";
                      index = -1;
                      added = 1;
                  }
                  if(StartingLine.contains(" end ")||StartingLine.contains(" end\n"))
                  {
                      currentLine = currentLine.substring(index);
                      c.setOriginal(StartingLine);
                      c.setLineNumber(i);
                      CodeList.add(c);
                      StartingLine = "";
                      index = -1;
                      added = 1;
                  }
                  //if there was a multiple line then check for possible white space
                  if (added == 1)
                  {
                      newChar = " ";
                      index = -1;
                      String Temp = "";
                      Code c2 = new Code();
                      //Cycle until end of string is hit or a non while space character is hit.
                      while((newChar.equals(" ")||newChar.equals("\t")||newChar.equals("\n")||newChar.equals("\\"))&&(index<currentLine.length()))
                      {
 
                          index++;
                          newChar = ""+currentLine.charAt(index);
                          Temp = Temp + newChar;
                          //if the next set of characters is a start of a comment
                          if (Temp.contains("//"))
                          {
                              String tempOriginal = "";
                              c2 = CodeList.get(CodeList.size()-1);
                              tempOriginal = c2.getOriginal();
                              c2.setOriginal(tempOriginal + currentLine);
                              currentLine = "";
                          }
 
                      //Cycle until end of string is hit or a non while space character is hit.
                      while((newChar.equals(" ")||newChar.equals("\t")||newChar.equals("\n"))&&(index<currentLine.length()))
                      {
                          index++;
                          newChar = ""+currentLine.charAt(index);
                      }
                      //if the end of string was hit set it to be the stop point;
                      if(currentLine.length()<index)
                      {
                          currentLine = "";
                      }
                      index = -1;
                  }
                  index++;
 
             }
 
         }
     }
 }
