 /**
  * ActionPanel.java
  *
  * Part of the jOculus project: https://github.com/Abstrys/jOculus
  *
  * Copyright (C) 2012 by Eron Hennessey
  */
 package abstrys.joculus;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import javax.swing.*;
 
 public class Joculus implements TextFileLoader.TextHandler, MarkdownProcessor.XhtmlHandler
 {
    enum OSType { OS_Unix, OS_MacOSX, OS_Windows, Unknown };
    static OSType os_type;
    static Settings settings;
    static JFrame frame = null;
 
    Joculus app_instance;
    File cur_file = null;
    TextFileLoader cur_file_loader = null;
    String cur_html;
    boolean markdown_failed;
    String file_contents;
    String file_pre = "";
    String file_ext = "";
    String base_url = ""; // the URL form of the file's parent directory.
    HTMLPanel html_panel = null;
    ActionPanel action_panel = null;
 
    public Joculus()
    {
    }
 
    public boolean init(String args[])
    {
       app_instance = this;
       String fpath = parseArgs(args);
       frame = new JFrame();
 
       // get the static parameters
       os_type = getOSType();
       settings = new Settings();
       settings.load();
 /*
       // set the application icon
       final String[] icon_names =
       {
          "jOculus-icon-256.png", "jOculus-icon-128.png",
          "jOculus-icon-96.png", "jOculus-icon-64.png", "jOculus-icon-48.png",
          "jOculus-icon-32.png", "jOculus-icon-16.png"
       };
       ArrayList<BufferedImage> icon_list = new ArrayList<BufferedImage>();
       for (String icon : icon_names)
       {
          BufferedImage i = null;
          try
          {
             i = ImageIO.read(getClass().getClassLoader().getResource("abstrys/joculus/res/" + icon));
          }
          catch (IOException ex)
          {
             showError(ex.getMessage());
          }
 
          if (i != null)
          {
             icon_list.add(i);
          }
       }
 
       frame.setIconImage(icon_list.get(3));
 */
 
       // Create the HTMLPanel, using the last saved window size (if available) to set the size.
       // Otherwise, use the default minimum size.
       Dimension window_size = new Dimension();
       html_panel = new HTMLPanel(settings.window_size_last);
 
       // Create the action panel.
       action_panel = new ActionPanel(this);
 
       if (fpath == null)
       {
          JOptionPane.showMessageDialog(
                  frame, UIStrings.ERROR_NO_FILE_SPECIFIED,
                  UIStrings.ERRORMSG_TITLE, JOptionPane.ERROR_MESSAGE);
          return false;
       }
 
       if (!setFile(fpath))
       {
          // the file was specified, but it could not be loaded. The user has been notified (in the setFile method).
          return false;
       }
 
       // Set up the application frame.
 
       Container cp = frame.getContentPane();
       cp.setLayout(new BorderLayout());
 
       cp.add(html_panel, BorderLayout.CENTER);
       cp.add(action_panel, BorderLayout.SOUTH);
 
       frame.pack();
       frame.setVisible(true);
 
       // handle window closing, passing execution to the onExit method.
       frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
       frame.addWindowListener(new WindowAdapter()
       {
          @Override
          public void windowClosing(WindowEvent e)
          {
             onExit();
          }
       });
 
 /*
       frame.addKeyListener(new KeyListener() {
 
          @Override
          public void keyTyped(KeyEvent ke)
          {
          }
 
          @Override
          public void keyPressed(KeyEvent ke)
          {
             if(ke.getModifiers() == java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
             {
                if(ke.getKeyCode() == KeyEvent.VK_O)
                {
                   action_panel.actionOpenFile(app_instance);
                }
                else if(ke.getKeyCode() == KeyEvent.VK_E)
                {
                   action_panel.actionEditFile(app_instance);
                }
             }
          }
 
          @Override
          public void keyReleased(KeyEvent ke)
          {
          }
       });*/
       return true;
    }
 
    public void run()
    {
 
    }
 
    private void onExit()
    {
       // if the user wants to save the window size on exit, do it.
       if (settings.window_size_remember)
       {
          settings.window_size_last = html_panel.getSize();
          settings.saveWindowSize();
       }
 
       System.exit(1);
    }
 
    public void refreshDisplay()
    {
       StringBuilder html_content = new StringBuilder();
 
       // invoke the processor to convert the file.
       html_content.append(UIStrings.XHTML_DECL);
       html_content.append(UIStrings.XHTML_HEAD_BEGIN);
       // TODO: add the stylesheet here.
       html_content.append(UIStrings.XHTML_HEAD_END);
       html_content.append(UIStrings.XHTML_BODY_BEGIN);
       html_content.append(cur_html);
       html_content.append(UIStrings.XHTML_END);
 
       base_url = "file://" + cur_file.getAbsoluteFile().getParent() + "/";
 
       html_panel.setHTML(html_content.toString(), base_url);
    }
 
    protected boolean setFile(String fpath)
    {
       if(cur_file_loader != null)
       {
          cur_file_loader.die();
          cur_file_loader = null;
       }
 
       if (fpath == null)
       {
          frame.setTitle(UIStrings.APPNAME + " - no file loaded");
          return false;
       }
 
       // grab the filename and extension while we're at it.
       String[] parts = fpath.split("\\.(?=[^\\.]+$)");
 
       file_pre = parts[0];
       file_ext = parts[1];
 
       if (frame != null)
       {
          frame.setTitle(UIStrings.APPNAME + " - " + file_pre);
       }
 
       cur_file_loader = new TextFileLoader(fpath, this);
       cur_file_loader.start();
 
       return true;
    }
 
    public static void main(String args[])
    {
       Joculus app_instance = new Joculus();
 
      String cp = System.getProperty("java.class.path");
      System.out.println("Classpath: " + cp);

       if (app_instance.init(args))
       {
       //   app_instance.run();
       }
    }
 
    /**
     * Returns the args. If a file was specified (required!), then it returns the file path in the return value.
     *
     * @param args the command-line arguments.
     * @return the filepath
     */
    private String parseArgs(String args[])
    {
       String fpath = null;
 
       for (String arg : args)
       {
          if (arg.startsWith("-"))
          {
             // TODO: maybe add something here.
          }
          else // expect a filename.
          {
             fpath = arg;
          }
       }
 
       return fpath;
    }
 
    private OSType getOSType()
    {
       String os_name = System.getProperty("os.name").toLowerCase();
       if(os_name.contains("mac"))
       {
          return OSType.OS_MacOSX;
       }
       else if(os_name.contains("nix") || os_name.contains("nux"))
       {
          return OSType.OS_Unix;
       }
       else if(os_name.contains("win"))
       {
          return OSType.OS_Windows;
       }
       else
       {
          return OSType.Unknown;
       }
    }
 
    public static void showError(String err_msg)
    {
       JOptionPane.showMessageDialog(null, err_msg, UIStrings.ERRORMSG_TITLE, JOptionPane.ERROR_MESSAGE);
    }
 
    // === === ===
    // Handlers for the TextFileLoader
    @Override
    public void textFileLoaded(File file, int wc, String contents)
    {
       cur_file = file;
       this.action_panel.setWordCount(wc);
 
       new Thread(new MarkdownProcessor(file, this)).start();
    }
 
    @Override
    public void textFileFailed(String error_text)
    {
       showError(error_text);
    }
 
    // === === ===
    // Handlers for the MarkdownProcessor
    @Override
    public void xhtmlSuccess(String xhtml)
    {
       cur_html = xhtml;
       markdown_failed = false;
       refreshDisplay();
    }
 
    @Override
    public void xhtmlFailure(String error_text)
    {
       markdown_failed = true;
       showError(error_text);
    }
 }
