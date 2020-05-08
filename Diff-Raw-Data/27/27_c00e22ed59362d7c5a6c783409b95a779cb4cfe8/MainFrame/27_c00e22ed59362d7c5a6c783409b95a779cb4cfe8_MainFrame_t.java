 /* Author of this file: Simon �agar, 2012, Ljubljana
  * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
  * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
  * or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
  */
 
 /*
  * Libraries used are TWL, slick-util, xpp3 and LWJGL.
  * Their licenses can be seen in one place by:
  *  - compiling and running the program and selecting the "Licensing" button or
  *  - by searching for String creditsString in this file.
  * The licenses should also be accessible online and some are included with the library folders.
  */
 package main;
 /**
  *@author Simon �agar, 63090355
 */
 
 import static org.lwjgl.opengl.GL11.GL_AMBIENT;
 import static org.lwjgl.opengl.GL11.GL_BLEND;
 import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
 import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
 import static org.lwjgl.opengl.GL11.GL_DIFFUSE;
 import static org.lwjgl.opengl.GL11.GL_EQUAL;
 import static org.lwjgl.opengl.GL11.GL_FALSE;
 import static org.lwjgl.opengl.GL11.GL_FRONT;
 import static org.lwjgl.opengl.GL11.GL_KEEP;
 import static org.lwjgl.opengl.GL11.GL_LIGHT0;
 import static org.lwjgl.opengl.GL11.GL_LIGHTING;
 import static org.lwjgl.opengl.GL11.GL_LINES;
 import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
 import static org.lwjgl.opengl.GL11.GL_MODULATE;
 import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_POSITION;
 import static org.lwjgl.opengl.GL11.GL_PROJECTION;
 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.GL_REPLACE;
 import static org.lwjgl.opengl.GL11.GL_SHININESS;
 import static org.lwjgl.opengl.GL11.GL_SMOOTH;
 import static org.lwjgl.opengl.GL11.GL_SPECULAR;
 import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_ENV;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_ENV_MODE;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glBlendFunc;
 import static org.lwjgl.opengl.GL11.glClearColor;
 import static org.lwjgl.opengl.GL11.glColor4f;
 import static org.lwjgl.opengl.GL11.glColorMask;
 import static org.lwjgl.opengl.GL11.glDepthMask;
 import static org.lwjgl.opengl.GL11.glDisable;
 import static org.lwjgl.opengl.GL11.glEnable;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glLight;
 import static org.lwjgl.opengl.GL11.glLineWidth;
 import static org.lwjgl.opengl.GL11.glLoadIdentity;
 import static org.lwjgl.opengl.GL11.glMaterial;
 import static org.lwjgl.opengl.GL11.glMatrixMode;
 import static org.lwjgl.opengl.GL11.glMultMatrix;
 import static org.lwjgl.opengl.GL11.glOrtho;
 import static org.lwjgl.opengl.GL11.glPopMatrix;
 import static org.lwjgl.opengl.GL11.glPushMatrix;
 import static org.lwjgl.opengl.GL11.glRotatef;
 import static org.lwjgl.opengl.GL11.glShadeModel;
 import static org.lwjgl.opengl.GL11.glStencilFunc;
 import static org.lwjgl.opengl.GL11.glStencilMask;
 import static org.lwjgl.opengl.GL11.glStencilOp;
 import static org.lwjgl.opengl.GL11.glTexCoord2f;
 import static org.lwjgl.opengl.GL11.glTexEnvf;
 import static org.lwjgl.opengl.GL11.glTranslatef;
 import static org.lwjgl.opengl.GL11.glVertex2f;
 import static org.lwjgl.opengl.GL11.glVertex3f;
 import static org.lwjgl.opengl.GL11.glViewport;
 import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
 import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
 import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
 import static org.lwjgl.opengl.GL20.glAttachShader;
 import static org.lwjgl.opengl.GL20.glCompileShader;
 import static org.lwjgl.opengl.GL20.glCreateShader;
 import static org.lwjgl.opengl.GL20.glDeleteProgram;
 import static org.lwjgl.opengl.GL20.glDeleteShader;
 import static org.lwjgl.opengl.GL20.glDetachShader;
 import static org.lwjgl.opengl.GL20.glGetShader;
 import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
 import static org.lwjgl.opengl.GL20.glLinkProgram;
 import static org.lwjgl.opengl.GL20.glShaderSource;
 import static org.lwjgl.opengl.GL20.glValidateProgram;
 import static org.lwjgl.opengl.ARBVertexBufferObject.*;
 import static tools.Tools.allocFloats;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.nio.FloatBuffer;
 
 import models.ObjOrPlyModel;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.Sys;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL20;
 import org.lwjgl.opengl.PixelFormat;
 import org.lwjgl.util.glu.GLU;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 import raa.pin.PinPanel;
 import tools.Quaternion;
 import tools.Vector;
 import de.matthiasmann.twl.Button;
 import de.matthiasmann.twl.EditField;
 import de.matthiasmann.twl.FileSelector;
 import de.matthiasmann.twl.FileSelector.Callback;
 import de.matthiasmann.twl.FolderBrowser;
 import de.matthiasmann.twl.GUI;
 import de.matthiasmann.twl.ListBox;
 import de.matthiasmann.twl.ScrollPane;
 import de.matthiasmann.twl.Scrollbar;
 import de.matthiasmann.twl.TextArea;
 import de.matthiasmann.twl.ToggleButton;
 import de.matthiasmann.twl.Widget;
 import de.matthiasmann.twl.model.DefaultEditFieldModel;
 import de.matthiasmann.twl.model.EditFieldModel;
 import de.matthiasmann.twl.model.JavaFileSystemModel;
 import de.matthiasmann.twl.model.SimpleChangableListModel;
 import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
 import de.matthiasmann.twl.textarea.SimpleTextAreaModel;
 import de.matthiasmann.twl.textarea.StyleSheet;
 import de.matthiasmann.twl.textarea.TextAreaModel;
 import de.matthiasmann.twl.theme.ThemeManager;
 
 /**
  * @author Simon �agar, 63090355
  *@version 0.4
  *@since 0.1
  */
 public class MainFrame extends Widget{
     static class NeckVeinsSettings implements Serializable{//similar to global variables except it can be saved
         private int resWidth, resHeight, bitsPerPixel, frequency;
         private boolean isFpsShown, fullscreen, stereoEnabled;
         private int stereoValue=0;
     }
     private static void saveSettings(){
         boolean saveSuccesful=false;
         String saveFileName=title+".save";
         try{FileOutputStream fos = new FileOutputStream(saveFileName, false);
             try{ObjectOutputStream saveOutStream=new ObjectOutputStream(fos);
                 saveOutStream.writeObject(settings);
                 saveOutStream.close();
                 saveSuccesful=true;
             }catch(IOException ee){}
         }catch(FileNotFoundException e){}
         if(!saveSuccesful) System.out.println("SaveUnsuccesful");
     }
     private static boolean loadSettings(){
         boolean loadSuccesful=false;
         String saveFileName=title+".save";
         try{
             FileInputStream fis = new FileInputStream(saveFileName);
             try{
                 ObjectInputStream saveInStream=new ObjectInputStream(fis);
                 try{Object aSave=saveInStream.readObject();
                     if(aSave instanceof NeckVeinsSettings){
                         settings=(NeckVeinsSettings)aSave;
                         loadSuccesful=true;
                     }
                 }catch(ClassNotFoundException eee){}
             }catch(IOException ee){}
         }catch(FileNotFoundException e){}
         if(!loadSuccesful){
             System.out.println("Loading settings file failed. Using default/current settings instead.");
             return false;
         }
         else{
             System.out.println("Loading settings file succeded.");
             return true;
         }
     }
     
     private static NeckVeinsSettings settings;
     
     //Widgets
     private static ThemeManager themeManager;
     private Button open;
     private ToggleButton pinToggleButton;
     private Button openPinButton;
     private Button newPinButton;
     private Button savePinButton;
     private Button saveAsPinButton;
     
     private Button displayModesButton, okayVideoSetting, cancelVideoSetting;
     private Button exit;
     private ToggleButton help;
     private ToggleButton credits;
     private ToggleButton stereoToggleButton;
     private Scrollbar stereoScrollbar;
     private ScrollPane helpScrollPane;
     private TextArea helpTextArea;
     private SimpleTextAreaModel stamHelp, stamCredits;
     private FileSelector fileSelector;
     private FileSelector fsAddImg;
     
     private ListBox displayModeListBox;
     private de.matthiasmann.twl.ToggleButton fullscreenToggle;
 
     private TextArea msgBoxContent;
     private TextArea msgBoxTitle;
     private EditField msgBoxInput;
     private Button msgBoxOkButton;
     private Button msgBoxCloseButton;
     private Button msgBoxCancelButton;
     
     private static boolean dialogOpened;
     private static boolean menuOpened = false;
 	//parameters
 	static float fovy=45;
 	static float zNear=1;
 	static float zFar=10000;
 	static String title="Raa3D";
 	////global variables
 	//GUI and display modes
 	static GUI gui;
 	static LWJGLRenderer renderer;
 	static DisplayMode[] displayModes;
 	static DisplayMode currentDisplayMode;
 	static String[]displayModeStrings;
 	static int selectedResolution=-1;
 	//veins model and shaders
 	static ObjOrPlyModel openedModel;
 	static Quaternion currentModelOrientation, addedModelOrientation;
 	static int[] shaderPrograms, vertexShaders, fragmentShaders;
 	static final int NUMBER_OF_SHADER_PROGRAMS=9;
 	static int activeShaderProgram=6;
 	//HUD
 	static Texture rotationCircle, circleGlow, movementCircle, rotationElipse, movementElipse, ellipseGlow;
 	//window variables
 	private static boolean isRunning;
 	private static long timePastFrame, fps, timePastFps, fpsToDisplay;
 	//camera pose
 	static Quaternion cameraOrientation;
 	static float cameraX=0, cameraY=0, cameraZ=0, cameraMoveSpeed=1.667f;
 	static double cameraRotationSpeed=(float)(72*Math.PI/180/60);
 	//variables for rotating the veins
 	static double[] screenPlaneInitialUpperLeft, screenPlaneInitialUpperRight, screenPlaneInitialLowerLeft, screenPlaneInitialLowerRight;
 	static double veinsRadius;
 	static double[] veinsGrabbedAt=null;
 	static int clickedOn=0;
 	static float rotationCircleAngle=0, rotationCircleDistance=0, ellipseSide=0;
 	//constants
 	static final int CLICKED_ON_NOTHING=0, CLICKED_ON_VEINS_MODEL=1, CLICKED_ON_ROTATION_CIRCLE=2, CLICKED_ON_MOVE_CIRCLE=3,
 	CLICKED_ON_ROTATION_ELLIPSE=4, CLICKED_ON_MOVE_ELLIPSE=5, CLICKED_ON_BUTTONS=6;
 	static final float ellipsef=1.1180339887498948482045868343656f;
 	
 	// PinPanel related variables
 	private static boolean loadingPinPanel;
 	private static PinPanel pinPanel;
 	
 	
 	//private static TextArea keyboardInputText = null;
 	//private static String inputContent = null;
 	private static boolean inputTextMode = false;
 	
 	// String containing default path of the model
 	private static String defaultPath = null;
 	private static String modelName = null;
 	/**
      * @since 0.4
      * @version 0.4
      */
 	public MainFrame(){
 	    fileSelector = new FileSelector();
         fileSelector.setTheme("fileselector");
         fileSelector.setVisible(false);
         de.matthiasmann.twl.model.JavaFileSystemModel fsm= JavaFileSystemModel.getInstance();
         fileSelector.setFileSystemModel(fsm);
         Callback cb = new Callback() {
             @Override
             public void filesSelected(Object[] files) {
                 setButtonsEnabled(true);
                 fileSelector.setVisible(false);
                 File file= (File)files[0];
                String path = file.getAbsolutePath();
                System.out.println("\nOpening file: "+path);
                 if (loadingPinPanel) {
                    loadPinPanel(path);
                     loadingPinPanel = false;
                 }
                 else {
                    defaultPath = path.substring(0, path.lastIndexOf(File.separatorChar)) + File.separatorChar;
                     modelName = file.getName();
                    infoBox("Info", "Loading model ... ");
                     loadModel(file.getAbsolutePath());
                    msgBoxDestroy();
                 }
             }
             @Override
             public void canceled() {
                 setButtonsEnabled(true);
                 fileSelector.setVisible(false);
                 
             }
         };
         fileSelector.addCallback(cb);
         add(fileSelector);
         
         
         open = new Button("Open model ...");
         open.setTheme("button");
         open.setTooltipContent("Open the dialog with the file chooser to select an .obj file.");
         open.addCallback(new Runnable(){
            @Override
         public void run(){
                openAFile();
            }
         });
         add(open);
         
         pinInit();
         
         
         
         exit = new Button("Exit");
         exit.setTheme("button");
         exit.setTooltipContent("Terminates this program.");
         exit.addCallback(new Runnable(){
            @Override
         public void run(){
                exitProgram(0);
            }
         });
         add(exit);
         
         stereoScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
         stereoScrollbar.setTheme("hscrollbar");
         stereoScrollbar.setTooltipContent("Sets the distance between eyes.");
         stereoScrollbar.setMinMaxValue(-1000, 1000);
         stereoScrollbar.setValue(settings.stereoValue);
         stereoScrollbar.addCallback(new Runnable(){
             @Override
             public void run(){
                 settings.stereoValue=stereoScrollbar.getValue();
             }
         });
         add(stereoScrollbar);
         
         stereoToggleButton = new ToggleButton("Stereo (3D)");
         stereoToggleButton.setTheme("togglebutton");
         stereoToggleButton.setTooltipContent("Toggles interlaced 3D picture. Requires an appropriate display.");
         stereoToggleButton.addCallback(new Runnable(){
            @Override
         public void run(){
                    settings.stereoEnabled=true;
                    stereoScrollbar.setVisible(true);
                    invalidateLayout();
                if(stereoToggleButton.isActive()){
                }else{
                    settings.stereoEnabled=false;
                    stereoScrollbar.setVisible(false);
                    invalidateLayout();
                }
            }
         });
         add(stereoToggleButton);
         if(settings.stereoEnabled){
             stereoScrollbar.setVisible(true);
             stereoToggleButton.setActive(true);
         }else{
             stereoScrollbar.setVisible(false);
             stereoToggleButton.setActive(false);
         }
         
         help = new ToggleButton("Help");
         help.setTheme("togglebutton");
         help.setTooltipContent("Shows controls.");
         help.addCallback(new Runnable(){
            @Override
         public void run(){
                if(help.isActive()){
                    helpTextArea.setModel(stamHelp);
                    helpScrollPane.setVisible(true);
                    setButtonsEnabled(false);
                    help.setEnabled(true);
                }else{
                    helpScrollPane.setVisible(false);
                    setButtonsEnabled(true);
                }
            }
         });
         add(help);
         
         credits = new ToggleButton("Licensing");
         credits.setTheme("togglebutton");
         credits.setTooltipContent("Shows authorship and licensing information.");
         credits.addCallback(new Runnable(){
            @Override
         public void run(){
                if(credits.isActive()){
                    helpTextArea.setModel(stamCredits);
                    helpScrollPane.setVisible(true);
                    setButtonsEnabled(false);
                    credits.setEnabled(true);
                }else{
                    helpScrollPane.setVisible(false);
                    setButtonsEnabled(true);
                }
            }
         });
         add(credits);
         
         String helpString="The Q, W, E, A, S and D keyboard buttons are used to look arround (rotate the camera):\n" +
         "   W - looks up\n   S - looks down\n   A - looks to the left\n   D - looks to the right\n" +
         "   Q - rotates the camera counterclockwise\n   E - rotates the camera clockwise\n" +
         "You can also use the ellipse on the right for more intuitive or finer rotations.\n\n" +
         "The arows keys and the R and F keyboard buttons are used to move (translate) the camera:\n" +
         "   Up Arrow - moves camera forward\n   Down Arrow - moves camera backwards\n" +
         "   Left Arrow - moves the camera left\n   Right Arrow - moves the camera right\n" +
         "   R - moves the camera upwards\n   F - moves the camera downwards\n" +
         "You can also use the ellipse on the right for more intuitive or finer movements.\n\n" +
         "You can scroll the mouse wheel to move closer or further away from the model.\n" +
         "You can click on an invisible sphere around the model and drag to rotate it.\n\n" +
         "You may change how the model is rendered with the following buttons:\n" +
         "   0 - use a fixed function pipeline\n" +
         "   1 - use a simple shader\n" +
         "   2 - like 1, but uses interpolated normals\n" +
         "   3 - like 2, but adds ambient light\n" +
         "   4 - like 3, but adds a specular component using Blinn�Phong model\n" +
         "   5 - like 3, but adds a specular component using Phong model (default)\n" +
         "   9 - toggles wireframe mode\n\n" +
         "You may apply a level of surface subdivision with the + button . Or switch back with the - button.\n" +
         "Maximal surface subdivision level that this application allows is 3. The starting one is 0.\n" +
         "Disclamer: Switching to a higher subdivision level for the first time constructs a new mesh.\n" +
         "Each such construction lasts about 6 times longer and is more likely to crash this application.\n\n" +
         "You can also activate 3D rendering for displays that support interlaced stereo.\n" +
         "The slider that appears below the \"Stereo (3D)\" button changes the distance between the eyes.\n" +
         "If the picture drawn for the right eye is shown to your left eye and vice versa, \n" +
         "try moving the slider on the other side of it's middle point.";
         String creditsString="Author(s):\n" +
         		" - Simon �agar\n" +
         		" - 3rd party libraries used, their respective licenses can be seen below.\n\n" +
         		"This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.\n" +
         		"To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/\n" +
         		" or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.\n" +
         		"\n" +
                 "This software uses LWJGL (Lightweight Java Game Library).\n" +
                 "LWJGL's license is quoted below between the two lines drawn with minus signs:\n" +
                 "-------------------------------------- LWJGL License: --------------------------------------\n" +
                 "\"Copyright (c) 2002-2008 Lightweight Java Game Library Project\n" +
                 "All rights reserved.\n\n" +
                 "Redistribution and use in source and binary forms, with or without\n" +
                 "modification, are permitted provided that the following conditions are\n" +
                 "met:\n\n" +
                 "* Redistributions of source code must retain the above copyright\n" +
                 "  notice, this list of conditions and the following disclaimer.\n\n" +
                 "* Redistributions in binary form must reproduce the above copyright\n" +
                 "  notice, this list of conditions and the following disclaimer in the\n" +
                 "  documentation and/or other materials provided with the distribution.\n\n" +
                 "* Neither the name of 'Light Weight Java Game Library' nor the names of\n" +
                 "  its contributors may be used to endorse or promote products derived\n" +
                 "  from this software without specific prior written permission.\n\n" +
                 "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS\n" +
                 "\"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED\n" +
                 "TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR\n" +
                 "PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR\n" +
                 "CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,\n" +
                 "EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,\n" +
                 "PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR\n" +
                 "PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF\n" +
                 "LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING\n" +
                 "NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS\n" +
                 "SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\"\n"+
                 "--------------------------------------------------------------------------------------------\n\n\n\n"+
                 
                 "This software uses Slick-Util library.\n" +
                 "Slick-Util library license is quoted below between the two lines drawn with minus signs:\n" +
                 "-------------------------------- Slick-Util library License: --------------------------------\n" +
                 "\"Copyright (c) 2007, Slick 2D\n\n" +
                 "All rights reserved.\n\n" +
                 "Redistribution and use in source and binary forms, with or without\n" +
                 "modification, are permitted provided that the following conditions are\n" +
                 "met:\n\n" +
                 "* Redistributions of source code must retain the above copyright\n" +
                 "  notice, this list of conditions and the following disclaimer.\n\n" +
                 "* Redistributions in binary form must reproduce the above copyright\n" +
                 "  notice, this list of conditions and the following disclaimer in the\n" +
                 "  documentation and/or other materials provided with the distribution.\n\n" +
                 "* Neither the name of Slick 2D nor the names of\n" +
                 "  its contributors may be used to endorse or promote products derived\n" +
                 "  from this software without specific prior written permission.\n\n" +
                 "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS\n" +
                 "\"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED\n" +
                 "TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR\n" +
                 "PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR\n" +
                 "CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,\n" +
                 "EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,\n" +
                 "PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR\n" +
                 "PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF\n" +
                 "LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING\n" +
                 "NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS\n" +
                 "SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\"\n"+
                 "--------------------------------------------------------------------------------------------\n\n\n\n"+
                 
         		"This software uses TWL (Themable Widget Library).\n" +
         		"TWL's license is quoted below between the two lines drawn with minus signs:\n" +
         		"--------------------------------------- TWL License: ---------------------------------------\n" +
         		"\"Copyright (c) 2008-2009, Matthias Mann\n\n" +
         		"All rights reserved.\n\n" +
         		"Redistribution and use in source and binary forms, with or without\n" +
         		"modification, are permitted provided that the following conditions are met:\n\n" +
         		"     * Redistributions of source code must retain the above copyright notice,\n" +
         		"       this list of conditions and the following disclaimer.\n" +
         		"     * Redistributions in binary form must reproduce the above copyright\n" +
         		"       notice, this list of conditions and the following disclaimer in the\n" +
         		"       documentation and/or other materials provided with the distribution.\n" +
         		"     * Neither the name of Matthias Mann nor the names of its contributors may\n" +
         		"       be used to endorse or promote products derived from this software\n" +
         		"       without specific prior written permission.\n\n" +
         		"THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS\n" +
         		"\"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT\n" +
         		"LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR\n" +
         		"A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR\n" +
         		"CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,\n" +
         		"EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,\n" +
         		"PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR\n" +
         		"PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF\n" +
         		"LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING\n" +
         		"NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS\n" +
         		"SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\"\n" +
         		"--------------------------------------------------------------------------------------------\n\n\n"+
                 "TWL uses XML Pull Parser 3 (XPP3).\n" +
                 "It's license requests the following two lines to be quoted:\n" +
                 "  \"This product includes software developed by the Indiana University \n" +
                 "  Extreme! Lab (http://www.extreme.indiana.edu/).\"\n\n" +
                 "XPP3's license is quoted below between the two lines drawn with minus signs:\n" +
                 "--------------------------------------- XPP3 License: ---------------------------------------\n" +
                 "\"Indiana University Extreme! Lab Software License\n\n" +
                 "Version 1.1.1\n\n" +
                 "Copyright (c) 2002 Extreme! Lab, Indiana University. All rights reserved.\n\n" +
                 "Redistribution and use in source and binary forms, with or without\n" +
                 "modification, are permitted provided that the following conditions are met:\n\n" +
                 "1. Redistributions of source code must retain the above copyright notice, \n" +
                 "   this list of conditions and the following disclaimer.\n\n" +
                 "2. Redistributions in binary form must reproduce the above copyright \n" +
                 "   notice, this list of conditions and the following disclaimer in \n" +
                 "   the documentation and/or other materials provided with the distribution.\n\n" +
                 "3. The end-user documentation included with the redistribution, if any, \n" +
                 "   must include the following acknowledgment:\n\n" +
                 "  \"This product includes software developed by the Indiana University \n" +
                 "  Extreme! Lab (http://www.extreme.indiana.edu/).\"\n\n" +
                 "Alternately, this acknowledgment may appear in the software itself, \n" +
                 "if and wherever such third-party acknowledgments normally appear.\n\n" +
                 "4. The names \"Indiana Univeristy\" and \"Indiana Univeristy Extreme! Lab\"\n" +
                 "must not be used to endorse or promote products derived from this \n" +
                 "software without prior written permission. For written permission, \n" +
                 "please contact http://www.extreme.indiana.edu/.\n\n" +
                 "5. Products derived from this software may not use \"Indiana Univeristy\" \n" +
                 "name nor may \"Indiana Univeristy\" appear in their name, without prior \n" +
                 "written permission of the Indiana University.\n\n" +
                 "THIS SOFTWARE IS PROVIDED \"AS IS\" AND ANY EXPRESSED OR IMPLIED\n" +
                 "WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF\n" +
                 "MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n" +
                 "IN NO EVENT SHALL THE AUTHORS, COPYRIGHT HOLDERS OR ITS CONTRIBUTORS\n" +
                 "BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR\n" +
                 "CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF\n" +
                 "SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR\n" +
                 "BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,\n" +
                 "WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR\n" +
                 "OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF\n" +
                 "ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\"\n" +
                 "--------------------------------------------------------------------------------------------\n";
         
         
         
         
         
         helpTextArea = new TextArea();
         helpTextArea.setTheme("textarea");
         stamHelp=new SimpleTextAreaModel(helpString);
         stamCredits=new SimpleTextAreaModel(creditsString);
         helpTextArea.setModel(stamHelp);
         
         helpScrollPane= new ScrollPane();
         helpScrollPane.setTheme("scrollpane");
         helpScrollPane.setVisible(false);
         add(helpScrollPane);
         helpScrollPane.setContent(helpTextArea);
         
         displayModesButton = new Button("Display Modes...");
         displayModesButton.setTheme("button");
         displayModesButton.setTooltipContent("Open the list with the available display modes.");
         displayModesButton.addCallback(new Runnable(){
            @Override
         public void run(){
                listDisplayModes();
            }
         });
         add(displayModesButton);
         
         okayVideoSetting = new Button("Okay");
         cancelVideoSetting = new Button("Cancel");
         fullscreenToggle = new ToggleButton("Toggle Fullscreen");
         
         okayVideoSetting.setVisible(false);
         cancelVideoSetting.setVisible(false);
         fullscreenToggle.setVisible(false);
         
         okayVideoSetting.setTheme("button");
         cancelVideoSetting.setTheme("button");
         fullscreenToggle.setTheme("togglebutton");
         okayVideoSetting.addCallback(new Runnable(){
             @Override
             public void run() {
                 confirmVideoSetting();
             }
         });
         cancelVideoSetting.addCallback(new Runnable(){
             @Override
             public void run() {
                 cancelVideoSetting();
             }
         });
         fullscreenToggle.addCallback(new Runnable(){
             @Override
             public void run() {
                 if(fullscreenToggle.isActive()){
                     try {
                         Display.setFullscreen(true);
                         Display.setVSyncEnabled(true);
                         settings.fullscreen=true;
                     } catch(LWJGLException e) {
                         e.printStackTrace();
                     }
                 }else{
                     try {
                         settings.fullscreen=false;
                         Display.setFullscreen(false);
                     } catch(LWJGLException e) {
                         e.printStackTrace();
                     }
                 }
             }
         });
         if(settings.fullscreen){
             try {
                 Display.setFullscreen(true);
                 Display.setVSyncEnabled(true);
                 fullscreenToggle.setActive(true);
             } catch(LWJGLException e) {
                 e.printStackTrace();
             }
         }else{
             try {
                 Display.setFullscreen(false);
                 fullscreenToggle.setActive(false);
             } catch(LWJGLException e) {
                 e.printStackTrace();
             }
         }
         add(okayVideoSetting);
         add(cancelVideoSetting);
         add(fullscreenToggle);
         
         displayModeListBox = new ListBox<String>();
         displayModeListBox.setTheme("listbox");
         displayModeListBox.setVisible(false);
         add(displayModeListBox);
         
         int i=0;
         selectedResolution=-1;
         for(DisplayMode displayMode:displayModes){
             if (displayMode.getWidth() == currentDisplayMode.getWidth() && displayMode.getHeight() == currentDisplayMode.getHeight() && displayMode.getBitsPerPixel() == currentDisplayMode.getBitsPerPixel() && displayMode.getFrequency() == currentDisplayMode.getFrequency()) {
                 selectedResolution=i;
             }
             displayModeStrings[i++]=String.format("%1$5d x%2$5d x%3$3dbit x%4$3dHz", displayMode.getWidth(), displayMode.getHeight(),displayMode.getBitsPerPixel(),displayMode.getFrequency());
         }
         
         SimpleChangableListModel<String> scListModel= new SimpleChangableListModel<String>();
         for(String str:displayModeStrings){
             scListModel.addElement(str);
         }
         
         displayModeListBox.setModel(scListModel);
         if(selectedResolution!=-1)displayModeListBox.setSelected(selectedResolution);
 	}
 	
 	/**
 	 * Init pin related swings
 	 */
     private void pinInit() {
         pinToggleButton = new ToggleButton("Pin panel");
         pinToggleButton.setTheme("togglebutton");
         pinToggleButton.setTooltipContent("Open the dialog with the file chooser to select an .r3dp file.");
         pinToggleButton.addCallback(new Runnable(){
            @Override
         public void run(){
                boolean enabled = pinToggleButton.isActive();
                menuOpened = enabled;
                setPinButtonsVisible(enabled);
                setButtonsEnabled(! enabled);
                pinToggleButton.setEnabled(true);
            }
         });
         add(pinToggleButton);
         
         openPinButton = new Button("Open ...");
         openPinButton.setTheme("button");
         openPinButton.setTooltipContent("Open the dialog with the file chooser to select an .r3dp file.");
         openPinButton.addCallback(new Runnable(){
            @Override
         public void run(){
                openAPinPanelFile();
                setPinButtonsVisible(false);
            }
         });
         add(openPinButton);
         
         newPinButton = new Button("New");
         newPinButton.setTheme("button");
         newPinButton.setTooltipContent("Open the dialog with the file chooser to select an .r3dp file.");
         newPinButton.addCallback(new Runnable(){
            @Override
         public void run(){
                newPinPanel();
                setPinButtonsVisible(false);
                pinToggleButton.setActive(false);
            }
         });
         add(newPinButton);
         
         savePinButton = new Button("Save");
         savePinButton.setTheme("button");
         savePinButton.setTooltipContent("Open the dialog with the file chooser to select an .r3dp file.");
         savePinButton.addCallback(new Runnable(){
            @Override
         public void run(){
                savePinPanel();
                setPinButtonsVisible(false);
                pinToggleButton.setActive(false);
            }
         });
         add(savePinButton);
         
         saveAsPinButton = new Button("Save as ...");
         saveAsPinButton.setTheme("button");
         saveAsPinButton.setTooltipContent("Open the dialog with the file chooser to select an .r3dp file.");
         saveAsPinButton.addCallback(new Runnable(){
            @Override
            public void run(){
                saveAsPinPanel();
                setPinButtonsVisible(false);
                pinToggleButton.setActive(false);
            }
         });
         //saveAsPinButton.setEnabled(false);
         add(saveAsPinButton);
         
         // File selectors
         de.matthiasmann.twl.model.JavaFileSystemModel fsm;
         Callback cb;
         
         
         
         fsAddImg = new FileSelector();
         fsAddImg.setTheme("fileselector");
         fsAddImg.setVisible(false);
         fsm= JavaFileSystemModel.getInstance();
         fileSelector.setFileSystemModel(fsm);
         cb = new Callback() {
             @Override
             public void filesSelected(Object[] files) {
                 setButtonsEnabled(true);
                 fsAddImg.setVisible(false);
                 File file= (File)files[0];
                 System.out.println("\nOpening file: "+file.getAbsolutePath());
                 
             }
             @Override
             public void canceled() {
                 setButtonsEnabled(true);
                 fsAddImg.setVisible(false);
                 
             }
         };
         fsAddImg.addCallback(cb);
         add(fsAddImg);
     }
     protected void initPinButtonsEnabled() {
         // always enabled
         openPinButton.setEnabled(true); 
         newPinButton.setEnabled(true);
         savePinButton.setEnabled(pinPanel != null && pinPanel.hasChanges());
         saveAsPinButton.setEnabled(pinPanel != null);
     }
     protected void setPinButtonsVisible(boolean visible) {
 	    openPinButton.setVisible(visible);
         savePinButton.setVisible(visible);
         saveAsPinButton.setVisible(visible);
         newPinButton.setVisible(visible);
     }
 	
     public void setButtonsEnabled(boolean enabled){
 	    dialogOpened=enabled;
 	    open.setEnabled(enabled);
 	    pinToggleButton.setEnabled(enabled);
         displayModesButton.setEnabled(enabled);
         help.setEnabled(enabled);
         credits.setEnabled(enabled);
 	}
 	
 	/**
      * @since 0.4
      * @version 0.4
      */
 	public void openAFile(){ 
         fileSelector.setVisible(true);
         setButtonsEnabled(false);
     }
 	
 
 	/**
 	 * Opens a pin panel file
 	 */
     public void openAPinPanelFile(){ 
         fileSelector.setVisible(true);
         setButtonsEnabled(false);
         setPinButtonsVisible(false);
         pinToggleButton.setActive(false);
     }
     
     /**
      * Creates a new pin panel if possible.
      */
     protected void newPinPanel() {
         // TODO Auto-generated method stub
         Runnable callback = new Runnable() {
             public void run() {
                 pinPanel = new PinPanel();
             }
         };
         if (pinPanel == null || pinPanel.hasChanges()) {
            if(defaultPath != null) {
                callback.run();
            }
            else {
                alertBox("Error", "No model opened.");
            }
         }
         else {
             confirmBox("New pin panel", "Are you sure? All unsaved changes will be lost.", callback, null);
         }
     }
 	
     /**
      * Saves a new pin panel if possible.
      */
     protected void savePinPanel() {
         // TODO Auto-generated method stub
        if (pinPanel != null && pinPanel.hasChanges()) {
            try {
                pinPanel.save();
            } catch(Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
     }
     
     /**
      * Creates a new pin panel if possible.
      */
     protected void saveAsPinPanel() {
         // TODO Auto-generated method stub
         if (pinPanel != null) {
             inputBox("Save as ... ", "Insert filename: ", new Runnable() {
                 public void run() {
                     String fname = msgBoxInput.getText();
                     if(fname.length() > 0) {
                        pinPanel.setFileLocation(defaultPath + File.separator + fname);
                         try {
                             pinPanel.save();
                         } catch(Exception e) {
                             // TODO Auto-generated catch block
                             e.printStackTrace();
                         }
                     }
                 }
             }, null);
         }
     }
     
 	/**
      * @since 0.4
      * @version 0.4
      */
     public void listDisplayModes(){ 
         okayVideoSetting.setVisible(true);
         cancelVideoSetting.setVisible(true);
         displayModeListBox.setVisible(true);
         fullscreenToggle.setVisible(true);
         setButtonsEnabled(false);
         displayModeListBox.setSelected(selectedResolution);
     }
     
     /**
      * @since 0.4
      * @version 0.4
      */
     public void confirmVideoSetting(){
         okayVideoSetting.setVisible(false);
         cancelVideoSetting.setVisible(false);
         displayModeListBox.setVisible(false);
         fullscreenToggle.setVisible(false);
         setButtonsEnabled(true);
         if(selectedResolution!=displayModeListBox.getSelected()){
             selectedResolution=displayModeListBox.getSelected();
             currentDisplayMode=displayModes[selectedResolution];
             try {
                 Display.setDisplayMode(currentDisplayMode);
                 settings.resWidth=currentDisplayMode.getWidth();
                 settings.resHeight=currentDisplayMode.getHeight();
             } catch(LWJGLException e) {
                 e.printStackTrace();
             }
             setupView();
             renderer.syncViewportSize();
             invalidateLayout();
         }
     }
     
     /**
      * @since 0.4
      * @version 0.4
      */
     public void cancelVideoSetting(){
         okayVideoSetting.setVisible(false);
         cancelVideoSetting.setVisible(false);
         displayModeListBox.setVisible(false);
         fullscreenToggle.setVisible(false);
         setButtonsEnabled(true);
     }
 	
 	/**
      * @since 0.4
      * @version 0.4
      */
 	@Override
     protected void layout(){
 	    open.adjustSize();
 	    pinToggleButton.adjustSize();
 	    openPinButton.adjustSize();
 	    newPinButton.adjustSize();
 	    savePinButton.adjustSize();
 	    saveAsPinButton.adjustSize();
 	    displayModesButton.adjustSize();
 	    stereoToggleButton.adjustSize();
 	    help.adjustSize();
 	    credits.adjustSize();
 	    exit.adjustSize();
         int openHeight=Math.max(25,settings.resHeight/18);
         
         int buttonWidth=settings.resWidth/7+1;
         open.setSize(buttonWidth, openHeight);
         open.setPosition(0, 0);
         
         pinToggleButton.setSize(buttonWidth, openHeight);
         pinToggleButton.setPosition(buttonWidth, 0);
         
         newPinButton.setSize(buttonWidth, openHeight);
         newPinButton.setPosition(buttonWidth, openHeight);
         newPinButton.setVisible(false);
         
         openPinButton.setSize(buttonWidth, openHeight);
         openPinButton.setPosition(buttonWidth, openHeight*2);
         openPinButton.setVisible(false);
         
         savePinButton.setSize(buttonWidth, openHeight);
         savePinButton.setPosition(buttonWidth, openHeight*3);
         savePinButton.setVisible(false);
         
         saveAsPinButton.setSize(buttonWidth, openHeight);
         saveAsPinButton.setPosition(buttonWidth, openHeight*4);
         saveAsPinButton.setVisible(false);
         
         displayModesButton.setPosition(buttonWidth*2, 0);
         displayModesButton.setSize(buttonWidth, openHeight);
         
         if(settings.stereoEnabled){
             stereoToggleButton.setPosition(buttonWidth*3, 0);
             stereoToggleButton.setSize(buttonWidth, openHeight/2);
             stereoScrollbar.setPosition(buttonWidth*3, openHeight/2);
             stereoScrollbar.setSize(buttonWidth, openHeight/2);
             //stereoScrollbar.setMinSize(settings.resWidth/36, openHeight);
         }else{
             stereoToggleButton.setPosition(buttonWidth*3, 0);
             stereoToggleButton.setSize(buttonWidth, openHeight);
             
             stereoScrollbar.setPosition(buttonWidth*3, openHeight);
             stereoScrollbar.setSize(buttonWidth, openHeight);
         }
         
         help.setPosition(buttonWidth*4, 0);
         help.setSize(buttonWidth, openHeight);
         credits.setPosition(buttonWidth*5, 0);
         credits.setSize(buttonWidth, openHeight);
         exit.setPosition(buttonWidth*6, 0);
         exit.setSize(settings.resWidth-buttonWidth*6, openHeight);
         
         
         int rlWidth=settings.resWidth*8/10;
         int rlHeight=settings.resHeight*6/10;
         displayModeListBox.setSize(rlWidth,rlHeight);
         displayModeListBox.setPosition(settings.resWidth/2-rlWidth/2, settings.resHeight/6);
         
         fullscreenToggle.adjustSize();
         int fullToggleWidth=Math.max(fullscreenToggle.getWidth(),settings.resWidth/6);
         fullscreenToggle.setSize(fullToggleWidth, openHeight);
         fullscreenToggle.setPosition(settings.resWidth/2-rlWidth/2, settings.resHeight*19/24);
         
         cancelVideoSetting.adjustSize();
         int cancelVideoSettingWidth=Math.max(cancelVideoSetting.getWidth(),settings.resWidth/6);
         cancelVideoSetting.setSize(cancelVideoSettingWidth, openHeight);
         cancelVideoSetting.setPosition(settings.resWidth/2+rlWidth/2-cancelVideoSettingWidth, settings.resHeight*19/24);
         
         okayVideoSetting.adjustSize();
         int okayVideoSettingWidth=Math.max(okayVideoSetting.getWidth(),settings.resWidth/6);
         okayVideoSetting.setSize(okayVideoSettingWidth, openHeight);
         okayVideoSetting.setPosition(settings.resWidth/2+rlWidth/2-cancelVideoSettingWidth-okayVideoSettingWidth, settings.resHeight*19/24);
         
         fileSelector.adjustSize();
         int fsHeight=settings.resHeight*19/24+openHeight-settings.resWidth/2+rlWidth/2;
         fileSelector.setSize(rlWidth,fsHeight);
         fileSelector.setPosition(settings.resWidth/2-rlWidth/2, settings.resHeight/6);
         
         
         fsAddImg.adjustSize();
         fsAddImg.setSize(rlWidth,fsHeight);
         fsAddImg.setPosition(settings.resWidth/2-rlWidth/2, settings.resHeight/6);
         
         helpScrollPane.setSize(rlWidth, fsHeight);
         helpScrollPane.setPosition(settings.resWidth/2-rlWidth/2, settings.resHeight/6);
         helpTextArea.setSize(rlWidth, fsHeight);
         
     }
 	
 	/**
 	 * @since 0.1
 	 * @version 0.4
 	 */
 	private static void mainLoop(){
 		init();
 		setupView();
 		fps=0;
 		timePastFrame=(Sys.getTime()*1000)/Sys.getTimerResolution();
 		timePastFps=timePastFrame;
 		fpsToDisplay=0;
 		while(!Display.isCloseRequested() && isRunning){
 			resetView();
 			if (!inputTextMode) pollInput();
 			render();
 			gui.update();
 			Display.update();
 			logic();
 			Display.sync(settings.frequency);
 		}
 	}
 	
 	/**
      * @since 0.4
      * @version 0.4
      */
 	private static void loadModel(String fileName){
 	    openedModel = null;
 	    System.gc();
 	    openedModel = new ObjOrPlyModel(fileName);
 	    System.gc();
 	    //Calculate the appropriate camera distance:
 	    //The following code takes the most extreme values on each coordinate
 	    //of all the specified vertices in the .obj file.
 	    //It uses the bigger distance (of two) from the average location on each axis
 	    //to calculate the radius of a circle that would surely enclose every vertex,
 	    //although allowing the radius to be slightly bigger than necessary.
 	    double d1=openedModel.minX-openedModel.centerx;
 	    double d2=openedModel.maxX-openedModel.centerx;
 	    double d3=openedModel.minY-openedModel.centery;
 	    double d4=openedModel.maxY-openedModel.centery;
 	    double d5=openedModel.minZ-openedModel.centerz;
 	    double d6=openedModel.maxZ-openedModel.centerz;
 	    d1*=d1;d2*=d2;d3*=d3;d4*=d4;d5*=d5;d6*=d6;
 	    d1=Math.max(d1, d2);
 	    d2=Math.max(d3, d4);
 	    d3=Math.max(d5, d6);
 	    d1=Math.sqrt(Math.max(Math.max(d1+d2, d2+d3), d1+d3));
 	    //
 	    double fovMin;//The smaller angle of view of the horizontal and vertical ones.
 	    if(settings.resWidth<settings.resHeight)fovMin=fovy*settings.resWidth/(double)settings.resHeight;else fovMin=fovy;
 	    fovMin=Math.PI*fovMin/180;//To radians.
 	    veinsRadius=d1/Math.sqrt(2);
 	    cameraZ=(float)(d1/Math.tan(fovMin/2));
 	    cameraX=0;cameraY=0;
 	    cameraOrientation=new Quaternion();
 	    
 	    double yAngle=Math.PI*fovy/360d;
 	    double xAngle=yAngle*settings.resWidth/settings.resHeight;
         double screenPlaneZ=-d1/Math.tan(fovMin/2);
         double screenPlaneY=Math.tan(yAngle)*(-screenPlaneZ);
         double screenPlaneX=Math.tan(xAngle)*(-screenPlaneZ);
         screenPlaneInitialUpperLeft=new double[]{-screenPlaneX, screenPlaneY, screenPlaneZ};
         screenPlaneInitialUpperRight=new double[]{screenPlaneX, screenPlaneY, screenPlaneZ};
         screenPlaneInitialLowerLeft=new double[]{-screenPlaneX, -screenPlaneY, screenPlaneZ};
         screenPlaneInitialLowerRight=new double[]{screenPlaneX, -screenPlaneY, screenPlaneZ};
 	    
         veinsGrabbedAt=null;
         double angle1= Math.PI*-90/180;
         double angle2= Math.PI*180/180;
         currentModelOrientation = Quaternion.quaternionFromAngleAndRotationAxis(angle1, new double[]{1,0,0});
         double[] v = Quaternion.quaternionReciprocal(currentModelOrientation).rotateVector3d(new double[]{0,1,0});
         currentModelOrientation=Quaternion.quaternionMultiplication(currentModelOrientation, Quaternion.quaternionFromAngleAndRotationAxis(angle2, v));
 	    addedModelOrientation = new Quaternion();
 	    
 	}
     private static void loadPinPanel(String filePath) {
 	    try {
             pinPanel = PinPanel.open(filePath);
         } catch(IOException e) {
             // TODO error message if pin could not be opened
             e.printStackTrace();
         }
 	}
 	
 	/**
 	 * @since 0.1
 	 * @version 0.4
 	 */
 	private static void init(){
 	    //load textures
 	    try {
             rotationCircle = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("main/rotationCircle.png"));
         } catch(IOException e) {
             System.err.println("Loading texture main/rotationCircle.png unsuccessful");
             e.printStackTrace();
         }
         try {
             circleGlow = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("main/circleGlow.png"));
         } catch(IOException e) {
             System.err.println("Loading texture main/circleGlow.png unsuccessful");
             e.printStackTrace();
         }
         try {
             movementCircle = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("main/movementCircle.png"));
         } catch(IOException e) {
             System.err.println("Loading texture main/movementCircle.png unsuccessful");
             e.printStackTrace();
         }
         try {
             rotationElipse = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("main/rotationElipse.png"));
         } catch(IOException e) {
             System.err.println("Loading texture main/rotationElipse.png unsuccessful");
             e.printStackTrace();
         }
         try {
             movementElipse = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("main/movementElipse.png"));
         } catch(IOException e) {
             System.err.println("Loading texture main/movementElipse.png unsuccessful");
             e.printStackTrace();
         }
         try {
             ellipseGlow = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("main/ellipseGlow.png"));
         } catch(IOException e) {
             System.err.println("Loading texture main/ellipseGlow.png unsuccessful");
             e.printStackTrace();
         }
 	}
 	
 	/**
      * @since 0.1
      * @version 0.1
      */
 	private static void drawHUD(){
 	    //prepare
         glTexEnvf(GL_TEXTURE_ENV,GL_TEXTURE_ENV_MODE,GL_MODULATE);
         glMatrixMode(GL_PROJECTION);
         glPushMatrix();
         glLoadIdentity();
         glOrtho(0, settings.resWidth, 0, settings.resHeight, 0.2f, 2);
         glMatrixMode(GL_MODELVIEW);
         glPushMatrix();
         glLoadIdentity();
         glDisable(GL_LIGHTING);
 	    glEnable(GL_TEXTURE_2D);
         glClearColor(0f, 0f, 0f, 0f);
         glEnable(GL_BLEND);
         glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
         
         //begin drawing
 	    float w=settings.resWidth;
         float h=settings.resHeight;
         float r=w/18;
         float offset=r*2/3;
         float x=w-offset-r;
         float y=h-h/18-offset-r;
         float x2=w-offset-r;
         float y2=h-h/18-2*offset-3*r;
         
         glColor4f(1,1,1,1);
         GL11.glBindTexture(GL_TEXTURE_2D, rotationElipse.getTextureID());
         glBegin(GL_QUADS);
         glTexCoord2f(1, 0);
         glVertex3f(x+1.5f*r, y+r, -1.0f);
         glTexCoord2f(0, 0);
         glVertex3f(x-1.5f*r, y+r, -1.0f);
         glTexCoord2f(0, 1);
         glVertex3f(x-1.5f*r, y-r, -1.0f);
         glTexCoord2f(1, 1);
         glVertex3f(x+1.5f*r, y-r, -1.0f);
         glEnd();
         GL11.glBindTexture(GL_TEXTURE_2D, movementElipse.getTextureID());
         glBegin(GL_QUADS);
         glTexCoord2f(1, 0);
         glVertex3f(x2+1.5f*r, y2+r, -1.0f);
         glTexCoord2f(0, 0);
         glVertex3f(x2-1.5f*r, y2+r, -1.0f);
         glTexCoord2f(0, 1);
         glVertex3f(x2-1.5f*r, y2-r, -1.0f);
         glTexCoord2f(1, 1);
         glVertex3f(x2+1.5f*r, y2-r, -1.0f);
         glEnd();
         
         if(clickedOn==CLICKED_ON_MOVE_ELLIPSE || clickedOn==CLICKED_ON_ROTATION_ELLIPSE){
             float x3=x, y3=y;
             if(clickedOn==CLICKED_ON_MOVE_ELLIPSE){x3=x2; y3=y2;}
             glPushMatrix();
             glTranslatef(x3, y3, 0);
             if(ellipseSide==0)glRotatef((180), 0, 0, 1);
             glTranslatef(-x3, -y3, 0);
             GL11.glBindTexture(GL_TEXTURE_2D, ellipseGlow.getTextureID());
             glColor4f(1,1,1, 0.5f);
             glBegin(GL_QUADS);
             glTexCoord2f(1, 0);
             glVertex3f(x3+1.5f*r, y3+r, -0.8f);
             glTexCoord2f(0, 0);
             glVertex3f(x3-1.5f*r, y3+r, -0.8f);
             glTexCoord2f(0, 1);
             glVertex3f(x3-1.5f*r, y3-r, -0.8f);
             glTexCoord2f(1, 1);
             glVertex3f(x3+1.5f*r, y3-r, -0.8f);
             glEnd();
             glPopMatrix();
         }
         glColor4f(1,1,1,1);
         GL11.glBindTexture(GL_TEXTURE_2D, rotationCircle.getTextureID());
         glBegin(GL_QUADS);
         glTexCoord2f(1, 0);
         glVertex3f(x+r, y+r, -0.6f);
         glTexCoord2f(0, 0);
         glVertex3f(x-r, y+r, -0.6f);
         glTexCoord2f(0, 1);
         glVertex3f(x-r, y-r, -0.6f);
         glTexCoord2f(1, 1);
         glVertex3f(x+r, y-r, -0.6f);
         glEnd();
         
         
       //begin drawing
         GL11.glBindTexture(GL_TEXTURE_2D, movementCircle.getTextureID());
         glBegin(GL_QUADS);
         glTexCoord2f(1, 0);
         glVertex3f(x2+r, y2+r, -0.6f);
         glTexCoord2f(0, 0);
         glVertex3f(x2-r, y2+r, -0.6f);
         glTexCoord2f(0, 1);
         glVertex3f(x2-r, y2-r, -0.6f);
         glTexCoord2f(1, 1);
         glVertex3f(x2+r, y2-r, -0.6f);
         glEnd();
         if(clickedOn==CLICKED_ON_ROTATION_CIRCLE || clickedOn==CLICKED_ON_MOVE_CIRCLE){
             if(clickedOn==CLICKED_ON_MOVE_CIRCLE){x=x2; y=y2;}
             glPushMatrix();
             glTranslatef(x, y, 0);
             glRotatef((float)(180*rotationCircleAngle/Math.PI), 0, 0, 1);
             glTranslatef(-x, -y, 0);
             GL11.glBindTexture(GL_TEXTURE_2D, circleGlow.getTextureID());
             glColor4f(1,1,1,rotationCircleDistance);
             glBegin(GL_QUADS);
             glTexCoord2f(1, 0);
             glVertex3f(x+r, y+r, -0.4f);
             glTexCoord2f(0, 0);
             glVertex3f(x-r, y+r, -0.4f);
             glTexCoord2f(0, 1);
             glVertex3f(x-r, y-r, -0.4f);
             glTexCoord2f(1, 1);
             glVertex3f(x+r, y-r, -0.4f);
             glEnd();
             glPopMatrix();
         }
         
         //exit "HUD" mode
         glDisable(GL_BLEND);
         glDisable(GL_TEXTURE_2D);
         glEnable(GL_LIGHTING);
         glMatrixMode(GL_PROJECTION);
         glPopMatrix();
         glMatrixMode(GL_MODELVIEW);
         glPopMatrix();
 	}
 	
 	/**
 	 * @since 0.1
 	 * @version 0.2
 	 */
 	private static void setupView(){
 		glEnable(GL_CULL_FACE);
 		glEnable(GL_DEPTH_TEST);
 		glViewport(0, 0, settings.resWidth, settings.resHeight);
 	    GL11.glMatrixMode(GL11.GL_PROJECTION);
 	    GL11.glLoadIdentity();
 	    GLU.gluPerspective(fovy, settings.resWidth/(float)settings.resHeight, zNear, zFar);
 		glShadeModel(GL_SMOOTH);
 		setCameraAndLight(0);
 	}
 	
 	/**
 	 * @since 0.1
 	 * @version 0.1
 	 */
 	private static void resetView(){
 	    glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 	}
 	
 	/**
 	 * @since 0.1
 	 * @version 0.1
 	 */
 	private static void setCameraAndLight(float offset){
 	    double v[]= new double[]{offset, 0, 0};
         v=cameraOrientation.rotateVector3d(v);
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		GL11.glLoadIdentity();
 		Quaternion worldOrientation = Quaternion.quaternionReciprocal(cameraOrientation);
 		glMultMatrix(worldOrientation.getRotationMatrix(false));
 		glTranslatef(-cameraX+(float)v[0], -cameraY+(float)v[1], -cameraZ+(float)v[2]);
 		
 		glEnable(GL_LIGHTING);
 		glEnable(GL_LIGHT0);
 		glLight(GL_LIGHT0,GL_POSITION,allocFloats(new float[]{0.0f, 1000.0f, 0.0f , 0.0f}));
 		glLight(GL_LIGHT0,GL_DIFFUSE,allocFloats(new float[]{1f,1f,1f,1}));
 		glLight(GL_LIGHT0,GL_AMBIENT,allocFloats(new float[]{0.3f,0.3f,0.3f,1}));
 		glLight(GL_LIGHT0,GL_SPECULAR,allocFloats(new float[]{1.0f, 1.0f, 1.0f, 1.0f}));
 	}
 	
 	public static void initStencil(){
         //Stencil
         glStencilMask(0x01);
         GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
         glEnable(GL_STENCIL_TEST);
         //disable writing colors and depth
         glColorMask(false, false, false, false);
         glDepthMask(false);
         //Make the stencil test always fail and on fail replace the appropriate value in stencil buffer to 1
         glStencilFunc(GL11.GL_NEVER, 1, 0x01);
         glStencilOp(GL_REPLACE, GL_KEEP, GL_KEEP);
         glStencilMask(0x01);//enable writing to bitplanes
         //prepare ortho drawing
         glMatrixMode(GL_PROJECTION);
         glPushMatrix();
         glLoadIdentity();
         glOrtho(0, settings.resWidth, 0, settings.resHeight, 0.2f, 3);
         glMatrixMode(GL_MODELVIEW);
         glPushMatrix();
         glLoadIdentity();
         glDisable(GL_LIGHTING);
         glDisable(GL_DEPTH_TEST);
         //draw the lines
         glTranslatef(0.375f,0.375f,-2);
         glColor4f(1,1,1,1);
         for(int i=0;i<settings.resHeight;i+=2){
             glLineWidth(1);
             glBegin(GL_LINES);
             glVertex2f(0,i);
             glVertex2f(settings.resWidth,i);
             glEnd();
         }
         //exit ortho drawing
         glMatrixMode(GL_PROJECTION);
         glPopMatrix();
         glMatrixMode(GL_MODELVIEW);
         glPopMatrix();
         glEnable(GL_DEPTH_TEST);
         //enable writing colors and depth and disable writing to bitplanes
         glColorMask(true, true, true, true);
         glDepthMask(true);
         glStencilMask(0x00);
     }
 	
 	private static void renderVeins(){
 	    if(openedModel!=null){
             glMatrixMode(GL_MODELVIEW);
             glPushMatrix();
             Quaternion compositeOrientation=Quaternion.quaternionMultiplication(currentModelOrientation, addedModelOrientation);
             FloatBuffer fb= compositeOrientation.getRotationMatrix(false);
             GL11.glMultMatrix(fb);
             
             glEnable(GL_LIGHTING);
             glColor4f(0.8f, 0.06667f, 0.0f, 1);
             glMaterial(GL_FRONT,GL_AMBIENT, allocFloats(new float[]{0.8f, 0.06667f, 0.0f, 1}));
             glMaterial(GL_FRONT,GL_DIFFUSE, allocFloats(new float[]{0.8f, 0.06667f, 0.0f, 1}));
             glMaterial(GL_FRONT,GL_SPECULAR, allocFloats(new float[]{0.0f, 0.0f, 0.0f, 1f}));
             glMaterial(GL_FRONT, GL_SHININESS, allocFloats(new float[]{0.5f, 0.25f, 0.25f, 0.25f}));
             openedModel.render(shaderPrograms[activeShaderProgram-1]);
             GL20.glUseProgram(0);
             glPopMatrix();
         }
 	}
 	
 	/**
 	 * @since 0.1
 	 * @version 0.4
 	 */
 	private static void render(){
 	    if(settings.stereoEnabled){
 	        float offset=settings.stereoValue/10f;
 	        initStencil();
 	        glStencilFunc(GL_EQUAL, 0, 0x01);
 	        setCameraAndLight(offset);
             renderVeins();
             glStencilFunc(GL_EQUAL, 1, 0x01);
             setCameraAndLight(-offset);
             renderVeins();
             glDisable(GL_STENCIL_TEST);
 	    }
 	    else{
 	        setCameraAndLight(0);
 	        renderVeins();
 	    }
 		//HUD
 	    GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
 		drawHUD();
 		if(settings.isFpsShown)Display.setTitle(title+" - FPS: "+fpsToDisplay);else	Display.setTitle(title);
 	}
 	
 	private static double[] getRaySphereIntersection(int x, int y){
 	    //figure out if the click on the screen intersects the circle that surrounds the veins model
         double[] d=getRayDirection(x, y);//get the direction of the "ray" cast from the camera location
         double[] e=new double[]{cameraX, cameraY, cameraZ};//a vector representing the camera location
         double[] c=new double[3];//the location of the sphere is the zero vector
         //partial calculations
         double[] eSc=Vector.subtraction(e, c);
         double dDPeSc=Vector.dotProduct(d, eSc);
         double discriminant = dDPeSc*dDPeSc-Vector.dotProduct(d, d)*(Vector.dotProduct(eSc, eSc)-veinsRadius*veinsRadius);
         if(discriminant<0){//in this case the mouse is not pressed near the veins sphere
             return null;
         }else{// in this case we hold the mouse on the sphere surrounding the veins model in some way
             double [] Sd=Vector.subtraction(new double[3], d);//partial calculation
             //t1 and t2 are the parameter values for vor "ray" expression e+t*d
             double t1=(Vector.dotProduct(Sd, eSc)+Math.sqrt(discriminant))/Vector.dotProduct(d, d);
             double t2=(Vector.dotProduct(Sd, eSc)-Math.sqrt(discriminant))/Vector.dotProduct(d, d);
             
             if(t2<0)return Vector.sum(e, Vector.vScale(d, t1));else return Vector.sum(e, Vector.vScale(d, t2));
         }
 	}
 	private static double[] getRayDirection(int x, int y){
 	    double[] tempUpperLeft=cameraOrientation.rotateVector3d(screenPlaneInitialUpperLeft);
 	    double[] tempLowerLeft=cameraOrientation.rotateVector3d(screenPlaneInitialLowerLeft);
 	    double[] tempLowerRight=cameraOrientation.rotateVector3d(screenPlaneInitialLowerRight);
 	    
 	    double[] leftToRight=Vector.subtraction(tempLowerRight, tempLowerLeft);
 	    leftToRight=Vector.vScale(leftToRight, (0.5d+x)/settings.resWidth);
 	    double[]rayD=Vector.sum(tempLowerLeft,leftToRight);
 	    
 	    double[] downToUp=Vector.subtraction(tempUpperLeft, tempLowerLeft);
 	    downToUp=Vector.vScale(downToUp, (0.5d+y)/settings.resHeight);
 	    
 	    rayD=Vector.sum(rayD,downToUp);
 	    
 	    return rayD;
 	}
 	
 	private static boolean isAAEnabled=false, wireframe=false;
 
 	/**
 	 * @since 0.1
 	 * @version 0.4
 	 */
 	private static void pollInput(){
 //	    if (keyboardInputText != null) {
 //	        while(Keyboard.next()){
 //	            if(Keyboard.getEventKeyState()){
 //	                int key = Keyboard.getEventKey();
 //	                
 //	                TextAreaModel tam = keyboardInputText.getModel();
 //	                
 //	            }
 //	        }
 //	        return;
 //	    }
 	    
 	    if(inputTextMode) return;
 	    
 		while(Keyboard.next()){
 			if(Keyboard.getEventKeyState()){//if a key was pressed (vs. released)
 				if(Keyboard.getEventKey()==Keyboard.KEY_TAB){
 					if(settings.isFpsShown)settings.isFpsShown=false;else settings.isFpsShown=true;
 				}else if(Keyboard.getEventKey()==Keyboard.KEY_1){
                     activeShaderProgram=1;
                 }else if(Keyboard.getEventKey()==Keyboard.KEY_2){
                     activeShaderProgram=2;
                 }else if(Keyboard.getEventKey()==Keyboard.KEY_3){
                     activeShaderProgram=3;
                 }else if(Keyboard.getEventKey()==Keyboard.KEY_4){
                     activeShaderProgram=4;
                 }else if(Keyboard.getEventKey()==Keyboard.KEY_5){
                     activeShaderProgram=5;
                 }else if(Keyboard.getEventKey()==Keyboard.KEY_6){
                     activeShaderProgram=6;
                 }else if(Keyboard.getEventKey()==Keyboard.KEY_7){
                     activeShaderProgram=7;
                 }else if(Keyboard.getEventKey()==Keyboard.KEY_8){
                     activeShaderProgram=8;
                 }else if(Keyboard.getEventKey()==Keyboard.KEY_9){
                     activeShaderProgram=9;
                 }else if(Keyboard.getEventKey()==Keyboard.KEY_0){
                     if(!(wireframe=!wireframe))GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
                     else GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
                 }else if(Keyboard.getEventKey()==Keyboard.KEY_ADD){
                     openedModel.increaseSubdivisionDepth();
                 }
                 else if(Keyboard.getEventKey()==Keyboard.KEY_SUBTRACT){
                     openedModel.decreaseSubdivisionDepth();
                 }else if(Keyboard.getEventKey()==Keyboard.KEY_9){
                     isAAEnabled=!isAAEnabled;
                 }
 			}
 		}
 		
 		
 		if(!dialogOpened || menuOpened )return;
 		int z=Mouse.getDWheel();
         if(z>0){
             cameraX*=0.8;
             cameraY*=0.8;
             cameraZ*=0.8;
         }else if(z<0){
             cameraX*=1.25;
             cameraY*=1.25;
             cameraZ*=1.25;
         }
         
 		//moving the camera
 		if(Keyboard.isKeyDown(Keyboard.KEY_W)){
 		  //create a vector representing the rotation axis
 		    Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(cameraRotationSpeed, new double[]{1,0,0});
 		    cameraOrientation=Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_S)){
 		    Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(cameraRotationSpeed, new double[]{-1,0,0});
             cameraOrientation=Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_A)){
 		    Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(cameraRotationSpeed, new double[]{0,1,0});
             cameraOrientation=Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_D)){
 		    Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(cameraRotationSpeed, new double[]{0,-1,0});
             cameraOrientation=Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_Q)){
 		    Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(cameraRotationSpeed, new double[]{0,0,1});
             cameraOrientation=Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_E)){
 		    Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(cameraRotationSpeed, new double[]{0,0,-1});
             cameraOrientation=Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_UP)){
 		    double v[]= new double[]{0, 0, -1};
 		    v=cameraOrientation.rotateVector3d(v);
             cameraX+=(float)v[0];
             cameraY+=(float)v[1];
             cameraZ+=(float)v[2];
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)){
 		    double v[]= new double[]{0, 0, 1};
             v=cameraOrientation.rotateVector3d(v);
             cameraX+=(float)v[0];
             cameraY+=(float)v[1];
             cameraZ+=(float)v[2];
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)){
 		    double v[]= new double[]{1, 0, 0};
             v=cameraOrientation.rotateVector3d(v);
             cameraX+=(float)v[0];
             cameraY+=(float)v[1];
             cameraZ+=(float)v[2];
         }
 		if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)){
 		    double v[]= new double[]{-1, 0, 0};
             v=cameraOrientation.rotateVector3d(v);
             cameraX+=(float)v[0];
             cameraY+=(float)v[1];
             cameraZ+=(float)v[2];
         }
 		if(Keyboard.isKeyDown(Keyboard.KEY_R)){
 		    double v[]= new double[]{0, 1, 0};
             v=cameraOrientation.rotateVector3d(v);
             cameraX+=(float)v[0];
             cameraY+=(float)v[1];
             cameraZ+=(float)v[2];
         }
 		if(Keyboard.isKeyDown(Keyboard.KEY_F)){
 		    double v[]= new double[]{0, -1, 0};
             v=cameraOrientation.rotateVector3d(v);
             cameraX+=(float)v[0];
             cameraY+=(float)v[1];
             cameraZ+=(float)v[2];
         }
 		
 		if(openedModel!=null){
 		    if(Mouse.isButtonDown(0)){
 		        //figure out if clicked on the HUD first
 		        float w=settings.resWidth;
 		        float h=settings.resHeight;
 		        float r=w/18;
 		        float offset=r*2/3;
 		        float x=w-offset-r;
 		        float y=h-h/18-offset-r;
 		        float x2=w-offset-r;
 		        float y2=h-h/18-2*offset-3*r;
 		        float f=ellipsef*r;
 		        
 		        if(clickedOn==CLICKED_ON_NOTHING){
 		            float distanceToRotationCircle=(x-Mouse.getX())*(x-Mouse.getX())+(y-Mouse.getY())*(y-Mouse.getY());
 		            float distanceToMoveCircle=(x2-Mouse.getX())*(x2-Mouse.getX())+(y2-Mouse.getY())*(y2-Mouse.getY());
 		            float distanceToRotationFoci=(float)(Math.sqrt((x-f-Mouse.getX())*(x-f-Mouse.getX())+(y-Mouse.getY())*(y-Mouse.getY()))+
 		                    Math.sqrt((x+f-Mouse.getX())*(x+f-Mouse.getX())+(y-Mouse.getY())*(y-Mouse.getY())));
 		            float distanceToMoveFoci=(float)(Math.sqrt((x2-f-Mouse.getX())*(x2-f-Mouse.getX())+(y2-Mouse.getY())*(y2-Mouse.getY()))+
                             Math.sqrt((x2+f-Mouse.getX())*(x2+f-Mouse.getX())+(y2-Mouse.getY())*(y2-Mouse.getY())));
 		            if(settings.resHeight-Mouse.getY()<settings.resHeight/18){
 		                clickedOn=CLICKED_ON_BUTTONS;
 		            }else if(distanceToRotationCircle<=r*r){
                         clickedOn=CLICKED_ON_ROTATION_CIRCLE;
                     }else if(distanceToMoveCircle<=r*r){
                         clickedOn=CLICKED_ON_MOVE_CIRCLE;
                     }else if(distanceToRotationFoci<=r*3f){
                         clickedOn=CLICKED_ON_ROTATION_ELLIPSE;
                     }else if(distanceToMoveFoci<=r*3f){
                         clickedOn=CLICKED_ON_MOVE_ELLIPSE;
                     }else{
                         veinsGrabbedAt=getRaySphereIntersection(Mouse.getX(), Mouse.getY());
                         addedModelOrientation=new Quaternion();
                         if(veinsGrabbedAt!=null)clickedOn=CLICKED_ON_VEINS_MODEL;
                     }
 		        }
 		        
 		        if(clickedOn==CLICKED_ON_VEINS_MODEL){
 		            double[] veinsHeldAt=getRaySphereIntersection(Mouse.getX(), Mouse.getY());
                     if(veinsHeldAt!=null){
                         double[] rotationAxis=Vector.crossProduct(veinsGrabbedAt, veinsHeldAt);
                         if(Vector.length(rotationAxis)>0){
                             rotationAxis=Vector.normalize(rotationAxis);
                             rotationAxis=Quaternion.quaternionReciprocal(currentModelOrientation).rotateVector3d(rotationAxis);
                             double angle=Math.acos(Vector.dotProduct(veinsGrabbedAt, veinsHeldAt)/(Vector.length(veinsGrabbedAt)*Vector.length(veinsHeldAt)));
                             addedModelOrientation= Quaternion.quaternionFromAngleAndRotationAxis(angle, rotationAxis);
                         }
                     }
 		        }
 		        if(clickedOn==CLICKED_ON_ROTATION_CIRCLE || clickedOn==CLICKED_ON_MOVE_CIRCLE){
 		            if(clickedOn==CLICKED_ON_MOVE_CIRCLE){x=x2;y=y2;}
 		            rotationCircleDistance=(x-Mouse.getX())*(x-Mouse.getX())+(y-Mouse.getY())*(y-Mouse.getY());
 		            rotationCircleAngle=(float)Math.atan2(Mouse.getY()-y, Mouse.getX()-x);
 		            rotationCircleDistance=(float)Math.sqrt(rotationCircleDistance);
 		            float upRotation=(Mouse.getY()-y);
 		            float rightRotation=(Mouse.getX()-x);
 		            if(rotationCircleDistance>r){
 		                upRotation/=rotationCircleDistance;
 		                rightRotation/=rotationCircleDistance;
 		            }else{
 		                upRotation/=r;
                         rightRotation/=r;
 		            }
 		            rotationCircleDistance=Math.min(rotationCircleDistance, r)/r;
 		            if(clickedOn==CLICKED_ON_ROTATION_CIRCLE){
 		                Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(cameraRotationSpeed*upRotation, new double[]{1,0,0});
 		                cameraOrientation=Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
 		                addRotation = Quaternion.quaternionFromAngleAndRotationAxis(cameraRotationSpeed*rightRotation, new double[]{0,-1,0});
 		                cameraOrientation=Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
 		            }else{
 		                double up[]= new double[]{0, 0, -upRotation};
 		                up=cameraOrientation.rotateVector3d(up);
 		                double right[]= new double[]{rightRotation, 0, 0};
 		                right=cameraOrientation.rotateVector3d(right);
 		                cameraX+=(float)(up[0]+right[0]);
 		                cameraY+=(float)(up[1]+right[1]);
 		                cameraZ+=(float)(up[2]+right[2]);
 		            }
 		        }
 		        if(clickedOn==CLICKED_ON_ROTATION_ELLIPSE){
 		            if(x-Mouse.getX()<=0){
 		                ellipseSide=0;
 		                Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(cameraRotationSpeed, new double[]{0,0,-1});
                         cameraOrientation=Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
 		            }else{
 		                ellipseSide=1;
 		                Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(cameraRotationSpeed, new double[]{0,0,1});
 		                cameraOrientation=Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
 		            }
 		        }
 		        if(clickedOn==CLICKED_ON_MOVE_ELLIPSE){
 		            double v[];
 		            if(x2-Mouse.getX()<=0){
 		                ellipseSide=0;
 		                v= new double[]{0, 1, 0};
 		            }else{
 		                ellipseSide=1;
 		                v= new double[]{0, -1, 0};
 		            }
 		            v=cameraOrientation.rotateVector3d(v);
 		            cameraX+=(float)v[0];
 		            cameraY+=(float)v[1];
 		            cameraZ+=(float)v[2];
                 }
 		    }else{
 		        clickedOn=CLICKED_ON_NOTHING;
 		        veinsGrabbedAt=null;
 		        currentModelOrientation=Quaternion.quaternionMultiplication(currentModelOrientation, addedModelOrientation);
 		        addedModelOrientation=new Quaternion();
 		    }
 		}
 		
 	}
 	
 	/**
      * @since 0.1
      * @version 0.1
      */
 	private static void logic(){
 		//update framerate and calculate time that passed since last frame
 		long time=(Sys.getTime()*1000)/Sys.getTimerResolution();
 		fps++;
 		if(time-timePastFps>=1000){
 			fpsToDisplay=fps;
 			fps=0;
 			timePastFps=time;
 			cameraOrientation=Quaternion.quaternionNormalization(cameraOrientation);
 			if(openedModel!=null){
 			    addedModelOrientation=Quaternion.quaternionNormalization(addedModelOrientation);
 			    currentModelOrientation=Quaternion.quaternionNormalization(currentModelOrientation);
 			}
 		}
 		timePastFrame=time;
 		
 	}
 	public static void exitProgram(int n){
 	    saveSettings();
 	    for(int i=0;i<NUMBER_OF_SHADER_PROGRAMS;i++){
 	        glDetachShader(shaderPrograms[i], vertexShaders[i]);
 	        glDeleteShader(vertexShaders[i]);
 	        glDetachShader(shaderPrograms[i], fragmentShaders[i]);
 	        glDeleteShader(fragmentShaders[i]);
 	        glDeleteProgram(shaderPrograms[i]);
 	    }
 	    gui.destroy();
 	    if(themeManager!=null)themeManager.destroy();
 	    Display.destroy();
 	    System.exit(n);
 	}
 	public static void prepareShaders(){
 	    shaderPrograms= new int[NUMBER_OF_SHADER_PROGRAMS];
 	    vertexShaders= new int[NUMBER_OF_SHADER_PROGRAMS];
 	    fragmentShaders= new int[NUMBER_OF_SHADER_PROGRAMS];
 	    String path="/main/";
 	    for(int i=1;i<=NUMBER_OF_SHADER_PROGRAMS;i++){
 	        shaderPrograms[i-1] = GL20.glCreateProgram();
 	        vertexShaders[i-1] = glCreateShader(GL_VERTEX_SHADER);
 	        fragmentShaders[i-1] = glCreateShader(GL_FRAGMENT_SHADER);
 	        StringBuilder vertexShaderSource = new StringBuilder();
 	        StringBuilder fragmentShaderSource= new StringBuilder();
 	        try{
 	            BufferedReader reader = new BufferedReader(new InputStreamReader(InputStreamReader.class.getResourceAsStream(path+"shader"+i+".vert")));
 	            String line;
 	            while( (line=reader.readLine())!=null){
 	                vertexShaderSource.append(line).append("\n");
 	            }
 	            reader.close();
 	        }catch(IOException e){
 	            System.err.println("Vertex shader"+i+" wasn't loaded properly.");
 	            exitProgram(1);
 	        }
 	        try{
 	            BufferedReader reader = new BufferedReader(new InputStreamReader(InputStreamReader.class.getResourceAsStream(path+"shader"+i+".frag")));
 	            String line;
 	            while( (line=reader.readLine())!=null){
 	                fragmentShaderSource.append(line).append("\n");
 	            }
 	            reader.close();
 	        }catch(IOException e){
 	            System.err.println("Fragment shader"+i+" wasn't loaded properly.");
 	            exitProgram(1);
 	        }
 	        glShaderSource(vertexShaders[i-1], vertexShaderSource);
 	        glCompileShader(vertexShaders[i-1]);
 	        if(glGetShader(vertexShaders[i-1], GL_COMPILE_STATUS)==GL_FALSE){
 	            System.err.println("Vertex shader"+i+" not compiled correctly");
 	        }
 	        
 	        glShaderSource(fragmentShaders[i-1], fragmentShaderSource);
 	        glCompileShader(fragmentShaders[i-1]);
 	        if(glGetShader(fragmentShaders[i-1], GL_COMPILE_STATUS)==GL_FALSE){
 	            System.err.println("Fragment shader"+i+" not compiled correctly");
 	        }
 	        
 	        glAttachShader(shaderPrograms[i-1], vertexShaders[i-1]);
 	        glAttachShader(shaderPrograms[i-1], fragmentShaders[i-1]);
 	        glLinkProgram(shaderPrograms[i-1]);
 	        glValidateProgram(shaderPrograms[i-1]);
 	        
 	        System.out.println("Vertex shader"+i+" info: "+glGetShaderInfoLog(vertexShaders[i-1], 999));
 	        System.out.println("Fragment shader"+i+" info: "+glGetShaderInfoLog(fragmentShaders[i-1], 999));
 	        System.out.println("Shader program"+i+" info: "+glGetShaderInfoLog(shaderPrograms[i-1], 999));
 	    }
 	}
 	
 	/**
 	 * @param args - has no function
 	 * @since 0.1
 	 * @version 0.4
 	 */
 	public static void main(String[] args) {
 	    cameraOrientation=new Quaternion();
         themeManager=null;
         boolean loadSuccessful;
         try{
             displayModes=Display.getAvailableDisplayModes();
             displayModeStrings=new String[displayModes.length];
             
             loadSuccessful=loadSettings();
             currentDisplayMode=Display.getDesktopDisplayMode();
             if(loadSuccessful){
                 for(DisplayMode mode:displayModes){
                     if(mode.getWidth()==settings.resWidth && mode.getHeight()==settings.resHeight && mode.getBitsPerPixel()==settings.bitsPerPixel && mode.getFrequency()==settings.frequency){
                         currentDisplayMode=mode;
                     }
                 }
             }else{
                 settings=new NeckVeinsSettings();
                 settings.isFpsShown=false;
                 settings.fullscreen=true;
                 settings.stereoEnabled=false;
                 settings.stereoValue=0;
             }
             settings.resWidth=currentDisplayMode.getWidth();
             settings.resHeight=currentDisplayMode.getHeight();
             settings.bitsPerPixel=currentDisplayMode.getBitsPerPixel();
             settings.frequency=currentDisplayMode.getFrequency();
             Display.setDisplayMode(currentDisplayMode);
             Display.setTitle(title);
             Display.create(new PixelFormat().withStencilBits(1));
             //Display.create();
             Display.setVSyncEnabled(true);
             
             GL11.glClearStencil(0);
             
             //The TWL part
             renderer = new LWJGLRenderer();
             MainFrame gameUI = new MainFrame();
             gui = new GUI(gameUI, renderer);
             themeManager = ThemeManager.createThemeManager(
                     MainFrame.class.getResource("simple.xml"), renderer);
             gui.applyTheme(themeManager);
             setupView();
             renderer.syncViewportSize();
             gameUI.invalidateLayout();
         }catch(LWJGLException e){
             e.printStackTrace();
             exitProgram(1);
         }catch(IOException ee){
             ee.printStackTrace();
             exitProgram(1);
         }
         System.out.println("OPENGL VERSION: "+GL11.glGetString(GL11.GL_VERSION)+"\n");
         prepareShaders();
         
 		isRunning=true;
 		mainLoop();
 		exitProgram(0);
 	}
 	
 	// User messaging section
 	
 	public void infoBox(String title, String message) {
 	    
 	    msgBoxContent = new TextArea();
 	    msgBoxTitle = new TextArea();
 	    
 	    SimpleTextAreaModel stmMsg = new SimpleTextAreaModel(message);
 	    SimpleTextAreaModel stmTit = new SimpleTextAreaModel(title);
 	    
 	    
 	    msgBoxContent.setModel(stmMsg);
 	    msgBoxTitle.setModel(stmTit);
 	    
 	    StyleSheet css = new StyleSheet();
 	    try {
             css.parse("p,div { text-align: center; }");
             msgBoxTitle.setStyleClassResolver(css);
         } catch(IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 	    
 	    
 	    msgBoxContent.adjustSize();
 	    msgBoxTitle.adjustSize();
 	    
 	    msgBoxTitle.setTheme("msgbox-title");
 	    msgBoxContent.setTheme("msgbox-content");
 	    
 	    msgBoxTitle.setSize(200, 20);
 	    msgBoxContent.setSize(200, 20);
         
 	    msgBoxTitle.setPosition(settings.resWidth/2 - 100, settings.resHeight/2 - 20);
 	    msgBoxContent.setPosition(settings.resWidth/2 - 100, settings.resHeight/2);
         
 	    
 	    add(msgBoxTitle);
 	    add(msgBoxContent);
 	    
 	    inputTextMode = true;
 	}
 	
 	
 	public void alertBox(String title, String message) {
 	    msgBoxContent = new TextArea();
         msgBoxTitle = new TextArea();
         
         SimpleTextAreaModel stmMsg = new SimpleTextAreaModel(message);
         SimpleTextAreaModel stmTit = new SimpleTextAreaModel(title);
         
         
         msgBoxContent.setModel(stmMsg);
         msgBoxTitle.setModel(stmTit);
         
         StyleSheet css = new StyleSheet();
         try {
             css.parse("p,div { text-align: center; }");
             msgBoxTitle.setStyleClassResolver(css);
         } catch(IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         
         msgBoxContent.adjustSize();
         msgBoxTitle.adjustSize();
         
         msgBoxTitle.setTheme("msgbox-title");
         msgBoxContent.setTheme("msgbox-content");
         
         msgBoxTitle.setSize(200, 20);
         msgBoxContent.setSize(200, 20);
         
         msgBoxTitle.setPosition(settings.resWidth/2 - 100, settings.resHeight/2 - 40);
         msgBoxContent.setPosition(settings.resWidth/2 - 100, settings.resHeight/2 - 20);
         
         msgBoxCloseButton = new Button("Close");
         msgBoxCloseButton.setPosition(settings.resWidth/2 - 50, settings.resHeight/2);
         msgBoxCloseButton.setSize(100, 40);
         msgBoxCloseButton.addCallback(new Runnable() {
             public void run() {
                 msgBoxDestroy();
             }
         });
         
         add(msgBoxCloseButton);
         add(msgBoxTitle);
         add(msgBoxContent);
 	}
 	
 	public void confirmBox(String title, String message, Runnable okFunction, Runnable cancelFunction) {
 	    msgBoxContent = new TextArea();
         msgBoxTitle = new TextArea();
         
         SimpleTextAreaModel stmMsg = new SimpleTextAreaModel(message);
         SimpleTextAreaModel stmTit = new SimpleTextAreaModel(title);
         
         
         msgBoxContent.setModel(stmMsg);
         msgBoxTitle.setModel(stmTit);
         
         msgBoxContent.adjustSize();
         msgBoxTitle.adjustSize();
         
         msgBoxTitle.setTheme("msgbox-title");
         msgBoxContent.setTheme("msgbox-content");
         
         msgBoxTitle.setSize(200, 20);
         msgBoxContent.setSize(200, 20);
         
         msgBoxTitle.setPosition(settings.resWidth/2 - 100, settings.resHeight/2 - 40);
         msgBoxContent.setPosition(settings.resWidth/2 - 100, settings.resHeight/2 - 20);
         
         msgBoxCancelButton = new Button("OK");
         msgBoxCancelButton.setPosition(settings.resWidth/2 - 100, settings.resHeight/2);
         msgBoxCancelButton.setSize(100, 40);       
         
         msgBoxOkButton = new Button("Cancel");
         msgBoxOkButton.setPosition(settings.resWidth/2, settings.resHeight/2);
         msgBoxOkButton.setSize(100, 40);
         
         msgBoxOkButton.addCallback(new Runnable() {
             public void run() {
                 msgBoxDestroy();
                 inputTextMode = false;
             }
         });
         if (okFunction != null) {
             msgBoxOkButton.addCallback(okFunction);
         }
         msgBoxCancelButton.addCallback(new Runnable() {
             public void run() {
                 msgBoxDestroy();
                 inputTextMode = false;
             }
         });
         if (cancelFunction != null) {
             msgBoxOkButton.addCallback(cancelFunction);
         }
         
         add(msgBoxCancelButton);
         add(msgBoxOkButton);
         add(msgBoxTitle);
         add(msgBoxContent);
 	}
 	
 	public void inputBox(String title, String message, Runnable okFunction, Runnable cancelFunction) {
 	    SimpleTextAreaModel stmMsg = new SimpleTextAreaModel(message);
         SimpleTextAreaModel stmTit = new SimpleTextAreaModel(title);
         
         EditFieldModel stmInput = new DefaultEditFieldModel();
         msgBoxContent = new TextArea();
         msgBoxTitle = new TextArea();
         msgBoxInput = new EditField(null, stmInput);
         
         
         
         msgBoxContent.setModel(stmMsg);
         msgBoxTitle.setModel(stmTit);
         //msgBoxInput.setModel(stmInput);
         
         msgBoxInput.setEnabled(true);
         msgBoxInput.setMultiLine(true);
         
         msgBoxContent.adjustSize();
         msgBoxTitle.adjustSize();
         msgBoxInput.adjustSize();
         
         msgBoxTitle.setTheme("msgbox-title");
         msgBoxContent.setTheme("msgbox-content");
         
         msgBoxTitle.setSize(200, 20);
         msgBoxContent.setSize(200, 20);
         msgBoxInput.setSize(200, 20);
         
         msgBoxInput.setEnabled(true);
         msgBoxInput.setCanAcceptKeyboardFocus(true);
         
         msgBoxTitle.setPosition(settings.resWidth/2 - 100, settings.resHeight/2 - 40);
         msgBoxContent.setPosition(settings.resWidth/2 - 100, settings.resHeight/2 - 20);
         msgBoxInput.setPosition(settings.resWidth/2 - 100, settings.resHeight/2 - 0);
         
         msgBoxCancelButton = new Button("OK");
         msgBoxCancelButton.setPosition(settings.resWidth/2 - 100, settings.resHeight/2 + 20);
         msgBoxCancelButton.setSize(100, 40);       
         
         msgBoxOkButton = new Button("Cancel");
         msgBoxOkButton.setPosition(settings.resWidth/2, settings.resHeight/2 + 20);
         msgBoxOkButton.setSize(100, 40);
         
         msgBoxOkButton.addCallback(new Runnable() {
             public void run() {
                 msgBoxDestroy();
                 inputTextMode = false;
             }
         });
         if (okFunction != null) {
             msgBoxOkButton.addCallback(okFunction);
         }
         msgBoxCancelButton.addCallback(new Runnable() {
             public void run() {
                 msgBoxDestroy();
                 inputTextMode = false;
             }
         });
         if (cancelFunction != null) {
             msgBoxOkButton.addCallback(cancelFunction);
         }
         
         add(msgBoxCancelButton);
         add(msgBoxOkButton);
         add(msgBoxTitle);
         add(msgBoxInput);
         add(msgBoxContent);
         inputTextMode = true;
         
         //inputContent = new String();
         //keyboardInputText = msgBoxInput;
     }
 	
 	public void msgBoxDestroy() {
	    inputTextMode = false;
         if (msgBoxContent != null) {
             removeChild(msgBoxContent);
             msgBoxContent.destroy();
             msgBoxContent = null;
         }
         if (msgBoxTitle != null) {
             removeChild(msgBoxTitle);
             msgBoxTitle.destroy();
             msgBoxTitle = null;
         }
         if (msgBoxCloseButton != null) {
             removeChild(msgBoxCloseButton);
             msgBoxCloseButton.destroy();
             msgBoxCloseButton = null;
         }
         if (msgBoxInput != null) {
             removeChild(msgBoxInput);
             msgBoxInput.destroy();
             msgBoxInput = null;
         }
         if (msgBoxOkButton != null) {
             removeChild(msgBoxOkButton);
             msgBoxOkButton.destroy();
             msgBoxOkButton = null;
         }
         if (msgBoxCancelButton != null) {
             removeChild(msgBoxCancelButton);
             msgBoxCancelButton.destroy();
             msgBoxCancelButton = null;
         }
     }
 }
