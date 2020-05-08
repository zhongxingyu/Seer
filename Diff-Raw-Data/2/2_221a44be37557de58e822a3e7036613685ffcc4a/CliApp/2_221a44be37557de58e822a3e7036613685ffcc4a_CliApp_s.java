 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.jebnf.cli;
 
 import de.weltraumschaf.jebnf.gfx.CreatorHelper;
 import de.weltraumschaf.jebnf.gfx.RailroadDiagram;
 import de.weltraumschaf.jebnf.gfx.RailroadDiagramImage;
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Command line application.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class CliApp implements Invokeable {
 
     /**
      * Default width in pixel.
      */
     private static final int WIDTH = 800;
 
     /**
      * Default height in pixel.
      */
     private static final int HEIGHT = 600;
 
     /**
     * IO Streams
      */
     private final IOStreams ioStreams;
 
     /**
      * Helper to create diagrams.
      */
     private final CreatorHelper helper = new CreatorHelper();
 
     /**
      * Command line options.
      */
     private final CliOptions options;
 
     /**
      * Initializes app with options and IO streams.
      *
      * @param options Command line options
      * @param streams IO streams.
      */
     public CliApp(final CliOptions options, final IOStreams streams) {
         this.options   = options;
         this.ioStreams = streams;
     }
 
     @Override
     public void run() {
         final RailroadDiagramImage img = new RailroadDiagramImage(WIDTH, HEIGHT, new File("./test.png"));
         final RailroadDiagram diagram = helper.createDiagram(img.getGraphics());
         diagram.setDebug(options.isDebug());
         img.setDiagram(diagram);
         img.paint();
 
         try {
             img.save();
         } catch (IOException ex) {
             ioStreams.getStderr().println("Can't write file!");
         }
     }
 
 }
