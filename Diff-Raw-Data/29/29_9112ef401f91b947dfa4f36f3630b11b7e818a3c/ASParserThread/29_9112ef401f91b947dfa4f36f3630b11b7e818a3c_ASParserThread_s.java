 //----------------------------------------------------------------------------
 // Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //
 // To contact the authors:
 // http://www.csc.liv.ac.uk/~bordini
 // http://www.inf.furb.br/~jomi
 //----------------------------------------------------------------------------
 
 package jIDE;
 
 import jason.asSyntax.parser.ParseException;
 import jason.asSyntax.parser.SimpleCharStream;
 import jason.asSyntax.parser.TokenMgrError;
 import jason.asSyntax.parser.as2j;
 import jason.asSyntax.parser.as2jTokenManager;
 
 import java.io.StringReader;
 
 import javax.swing.text.Document;
 
 /** a parser thread to compile AS sources while being edited */
 public class ASParserThread extends Thread {
     
     as2j    fASParser;
     
     JasonID fJasonID;
     
     boolean fStop = false;
     boolean fOk = false;
     boolean fForegroundCompilation = false;
     boolean forDone = true;
     boolean fCompilationDone = false;
 
     int fErrorLine = -1;
     
     ASParserThread(String name, JasonID jasonID) {
     	super(name);
         this.fJasonID = jasonID;
     }
     ASParserThread(JasonID jasonID) {
     	this("ASParserThread", jasonID);
     }
     
     /** returns the line number that contains error, -1 in case there is no errors. Only for current tab */
     public int getErrorLine() {
     	return fErrorLine;
     }
     
     void createParser() {
         try {
         	fASParser = new as2j(new StringReader(""));
         } catch (Exception ex) {
         	System.err.println("error creating parser!"+ex);
         	ex.printStackTrace();
         	fASParser = null;
         }    	
     }
     
     boolean parseTab(int tabIndex) {
     	if (fASParser == null) {
     		createParser();
     		if (fASParser == null) {
     			return false;
     		}
     	}
         ASEditorPane editorPanel = null;
         Document doc;
         SimpleCharStream inStream = null;
         
         // compile
         try {
             editorPanel = (ASEditorPane)fJasonID.tab.getComponentAt(tabIndex);
             if (RunCentralisedMAS.logPropFile.startsWith(editorPanel.getFileName())) {
             	return true;
             }
             if (!editorPanel.needsParsing && !fForegroundCompilation) {
                 return true;
             }
             if (fForegroundCompilation) {
                 System.out.print("Parsing "+editorPanel.getFileName()+"...");
             }
             doc  = editorPanel.editor.getDocument();
             String text = doc.getText(0, doc.getLength());
             
             inStream = new SimpleCharStream(new StringReader(text));
             fASParser.ReInit(new as2jTokenManager(inStream));
             fASParser.ag(null);
             
             if (fForegroundCompilation) {
                 System.out.println(" parsed successfully!");
             }
 
             fErrorLine = -1; // set no error!
             editorPanel.syntaxHL.paintLine(); //errorOffSet); // 1 char was inserted
             
             return true;
         } catch (ParseException ex) {
             if (fForegroundCompilation) {
                 System.out.println("\nas2j: parsing errors found... \n" + ex);
             } else {
             	if (fJasonID.tab.getSelectedIndex() == tabIndex && ex.currentToken != null) {
             		//jasonID.updateTabTitle(tabIndex, editorPanel, "!line "+ex.currentToken.beginLine);
             		//System.out.println("error line is "+ex.currentToken.beginLine+" buf bline="+inStream.getBeginLine()+" buf eline="+inStream.getEndLine());
 
             		// error is defined by stream line
             		int[][] expected = ex.expectedTokenSequences;
             		for (int i=0; i<expected.length; i++) {
             			for (int j=0; j<expected[i].length; j++) {
             				if (expected[i][j] == as2j.EOF) {
                         		fErrorLine = inStream.getBeginLine();
                         		editorPanel.syntaxHL.paintLine();
                         		return false;
             				}
             			}
             		}
 
             		// error is defined by las token
             		fErrorLine = ex.currentToken.beginLine;
             		if (fErrorLine == 0) { // first token problem
             			fErrorLine = 1;
             		}
             		editorPanel.syntaxHL.paintLine();
             	}
             }
         } catch (TokenMgrError ex) {
             if (fForegroundCompilation) {
                 System.out.println("\nas2j: error parsing tokens ... \n" + ex);
             } else {
                 int p = ex.toString().indexOf("line");
                 int v = ex.toString().indexOf(", ", p);
                 if (p > 0 && v > p) {
             		fErrorLine = Integer.parseInt(ex.toString().substring(p+4,v).trim());
             		//System.out.println("error line is "+ex.currentToken.beginLine+" buf bline="+inStream.getBeginLine()+" buf eline="+inStream.getEndLine()+" offset="+errorOffSet);
             		editorPanel.syntaxHL.paintLine();
                    //jasonID.updateTabTitle(tabIndex, editorPanel, "!line "+);
                 }
             }
         } catch (Exception ex) {
         	System.err.println("Unexpected error "+ex);
             ex.printStackTrace();
         } finally {
             editorPanel.needsParsing = false;
         }
         return false;
     }
     
     void parse() {
         try {
             fOk = true;
             fCompilationDone = false;
             for (int tabIndex=1; tabIndex < fJasonID.tab.getComponentCount(); tabIndex++) {
                 fOk = parseTab(tabIndex);
                 if (!fOk) {
                     break; 
                 }
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         } finally {
             fCompilationDone = true;
             stopWaiting();
         }
     }
     
     synchronized void waitSomeTime() {
         try {
             wait(5000); // waits 2 seconds
         } catch (Exception e) { }
     }
     
     synchronized void waitCompilation() {
     	int trycount = 0;
         while (!fCompilationDone && trycount < 10) {
             try {
                 wait(500); // waits 1/2 seconds
                 trycount++;
             } catch (Exception e) { }
         }
         if (trycount >= 10) {
         	System.out.println("stop waiting compilation, some unexpected error ocorred");
         	fOk = false;
         }
     }
     
     synchronized void stopWaiting() {
         notifyAll();
     }
     
     synchronized public boolean foregroundCompile() {
         fForegroundCompilation = true;
         fCompilationDone = false;
         stopWaiting(); // wakeup this thread
         waitCompilation(); // waits the end of compilation
         fForegroundCompilation = false;
         return fOk;
     }
     
     public void stopParser() {
         fStop = true;
         stopWaiting();
     }
 
    synchronized public void run() {
         createParser();
         while (!fStop) {
             if (!fStop) {
                 parse();
             }
             waitSomeTime();
         }
     }
 }
