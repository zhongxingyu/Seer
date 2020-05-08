 package memeograph;
 
 /**
  * Seriously, this is the whole program in a nutshell.
  * Config takes the args from the commandline and from default.properties
  *  - Look into Config to see how the GraphRenderer and Generator are chosen
  *
  * The GraphRenderer actually gets created first. Since the renderer is mostly
  * a GUI (The Processing Applet) it can be useful to have it pop up first
  * and let the user know what the graph generator is about to get it to work.
  * You can also get clever and use the renderer to maybe set some options for
  * the generator by blocking in init() until you're ready.
  *
  * The GraphGenerator gets going and starts to generate a graph that
  * gets passed to the Renderer.
  *
  * The program then exits.
  *
  */
 public class Memeographer {
 
    public static void main(String[] args) {
        Config config = Config.createConfig(args);
 
        GraphRenderer renderer = config.getRenderer();
        renderer.init();
 
        GraphGenerator generator = config.getGenerator();
        generator.start();
 
        renderer.setGraphs(generator.getGraphs());
    }
 
 }
