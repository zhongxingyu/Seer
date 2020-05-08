 package com.hifiremote.jp1;
 
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 
 public class RemoteManager
 {
   protected RemoteManager(){}
 
   public File loadRemotes( File loadPath )
     throws Exception
   {
     File[] files = new File[ 0 ];
     File dir = loadPath;
     FilenameFilter filter = new FilenameFilter()
     {
       public boolean accept( File dir, String name )
       {
         return name.toLowerCase().endsWith( ".rdf" );
       }
     };
 
     while ( files.length == 0 )
     {
       files = dir.listFiles( filter );
       if ( files.length == 0 )
       {
         JOptionPane.showMessageDialog( null, "No RDF files were found!",
                                        "Error", JOptionPane.ERROR_MESSAGE );
         JFileChooser chooser = new JFileChooser( dir );
         chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
 //        chooser.setFileFilter( new KMDirectoryFilter());
         chooser.setDialogTitle( "Choose the directory containing the RDFs" );
         int returnVal = chooser.showOpenDialog( null );
         if ( returnVal != JFileChooser.APPROVE_OPTION )
           System.exit( -1 );
         else
           dir = chooser.getSelectedFile();
       }
     }
     
     Vector work = new Vector();
     for ( int i = 0; i < files.length; i++ )
     {
       File rdf = files[ i ];
 
       Remote r = new Remote( rdf );
       work.add( r );
       for ( int j = 1; j < r.getNameCount(); j++ )
         work.add( new Remote( r, j ));
     }
     remotes = ( Remote[] )work.toArray( remotes );
     Arrays.sort( remotes );
     
     return dir;
   }
 
   public Remote[] getRemotes(){ return remotes; }
 
   public static RemoteManager getRemoteManager()
   {
     return remoteManager;
   }
 
   public Remote findRemoteByName( String name )
     throws Exception
   {
    System.err.println( "Seraching for remote with name " + name );
    if ( name == null )
      return null;
     Remote remote = null;
     int index = Arrays.binarySearch( remotes, name );
     if ( index < 0 )
     {
       // build a list of similar remote names, and ask the user to pick a match.
       // First check if there is a slash in the name;
       String[] subNames = new String[ 0 ];
       int slash = name.indexOf( '/' );
       if ( slash != -1 )
       {
         int count = 2;
         while (( slash = name.indexOf( '/', slash + 1 )) != -1 )
           count++;
         subNames = new String[ count ];
         StringTokenizer nameTokenizer = new StringTokenizer( name, " /" );
         for ( int i = 0; i < count; i++ )
         {
           subNames[ i ] = nameTokenizer.nextToken();
         }
       }
       else
       {
         subNames = new String[ 1 ];
         StringTokenizer nameTokenizer = new StringTokenizer( name );
         subNames[ 0 ] = nameTokenizer.nextToken();
       }
       Vector similarRemotes = new Vector();
       for ( int i = 0; i < remotes.length; i++ )
       {
         for ( int j = 0; j < subNames.length; j++ )
         {
           if ( remotes[ i ].getName().indexOf( subNames[ j ]) != -1 )
           {
             similarRemotes.add( remotes[ i ]);
             break;
           }
         }
       }
 
       Object[] simRemotes = null;
       if ( similarRemotes.size() > 0 )
         simRemotes = similarRemotes.toArray();
       else
         simRemotes = remotes;
 
       String message = "Could not find an exact match for the remote \"" + name + "\".  Choose one from the list below:";
 
       Object rc = ( Remote )JOptionPane.showInputDialog( null,
                                                          message,
                                                          "Unknown Remote",
                                                          JOptionPane.ERROR_MESSAGE,
                                                          null,
                                                          simRemotes,
                                                          simRemotes[ 0 ]);
       if ( rc == null )
         return remote;
       else
         remote = ( Remote )rc;
     }
     else
       remote = remotes[ index ];
     remote.load();
     return remote;
   }
 
   public Remote[] findRemoteBySignature( String signature ){ return null; }
 
   private static RemoteManager remoteManager = new RemoteManager();
 
   private Remote[] remotes = new Remote[ 0 ];
 }
