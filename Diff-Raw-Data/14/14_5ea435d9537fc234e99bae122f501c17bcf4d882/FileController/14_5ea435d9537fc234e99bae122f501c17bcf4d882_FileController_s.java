 package database.control.file;
 
 import database.control.ErrorHolder;
 import java.awt.Component;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.print.DocFlavor;
 
 public class FileController {
 
     private FileDialogController dialogCtlr;
 
     public FileController () {
         dialogCtlr = new FileDialogController();
     }
 
     public String readFile ( Component parentComponent, ErrorHolder errHolder ) {
 
         File file = dialogCtlr.showDialogAndGetFile( parentComponent );
         StringBuilder sb = new StringBuilder();
 
         if ( file == null )
             return null;
 
         try {
            BufferedReader input = new BufferedReader( new FileReader( file ) );
 
             String tmp;

             while ( ( tmp = input.readLine() ) != null )
                 sb.append( tmp ).append( "\n" );
 
             input.close();
 
         } catch ( FileNotFoundException ex ) {
             errHolder.addError( "Файл не знайдено." );
             return null;
 
         } catch ( IOException e ) {
             errHolder.addError( "З файлом бази даних щось сталось.\nНе хочу на дивитися на це страхiття..." );
             return null;
         } finally {
         }
 
         return sb.toString();
     }
 
     public void writeFile ( Component parentComponent, ErrorHolder errHolder, String content ) {
 
         File file = dialogCtlr.showDialogAndGetFile( parentComponent );
 
         if ( file == null )
             return;
 
         try {
            BufferedWriter output = new BufferedWriter( new FileWriter( file ) );
             output.write( content );
             output.close();
 
         } catch ( FileNotFoundException ex ) {
             errHolder.addError( "Файл не знайдено." );
 
         } catch ( IOException e ) {
             errHolder.addError( "З файлом бази даних щось сталось.\nНе хочу на дивитися на це страхiття..." );
         }
     }
 }
