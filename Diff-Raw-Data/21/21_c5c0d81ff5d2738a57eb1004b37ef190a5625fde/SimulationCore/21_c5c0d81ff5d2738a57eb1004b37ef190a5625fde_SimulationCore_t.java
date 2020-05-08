 package presentacion;
 
 import Negocio.BusinessSimulation;
 import Negocio.Obstruction;
 import Negocio.ServicePoint;
 import Negocio.Transport;
 import processing.core.PApplet;
 import controlP5.*;
 import javax.swing.ImageIcon;
 import processing.core.PImage;
 
 public class SimulationCore extends PApplet {
 
     ControlP5 cp5;
     private PImage iconCursor;
     private Accordion accordion;
     private int c = color(0, 160, 100);
     private PImage TType1;
     private PImage TType2;
     private PImage TType3;
     private PImage TType4;
     private PImage OType1;
     private PImage OType2;
     private PImage SPType1;
     private PImage SPType2;
     private PImage SPType3;
     private String TYPE_TRANSPORT;
     private int TYPE_OBSTRUCTION;
     private String TYPE_SERVICEPOINT;
     private int WindowWidth = 1200;
     private int WindowHeight = 630;
     private int ModuleMappedWidth = 42;
     private int ModuleMappedHeight = 35;
     private int PanelWidth = 168;
     private int PanelHeight = 630;
     private int ClearanceWidth = 24;//La holgura surge del numero de columnas
     private int ClearanceHeight = 18;//La holgura surge del numero de filas
     private boolean pause = true;
     private boolean flag = true;
 
     public void setup() {
         size(WindowWidth, WindowHeight);
         smooth();
         noStroke();
 
         cp5 = new ControlP5(this);
 
         loadGraphics();
         optionCreate();
         changeIcon();
         changePointer();
         buttonsCreate();
         setUpEnviroment();
     }
 
     public void draw() {
         background(250);
         initMap();
         fill(c);
 
         //float s1 = cp5.getController("hello").getValue();
         //ellipse(850, 300, s1, s1);
         //text("Este es el simulador de envío", 500, 200, 500, 500);
 
         //float s2 = cp5.getController("world").getValue();
         //ellipse(450, 300, s2, s2);
 
         //El parametro que recibe nextStep indica el punto de servicio al que se hace referencia
         if (pause == false && !BusinessSimulation.getInstance().nextStep(1)) {
             drawTransport();
         }
         drawServicePoint();
         drawObstruction();
     }
 
     void buttonsCreate() {
         cp5.addButton("Iniciar")
                 .setValue(0)
                 .setPosition(10, 550)
                 .setSize(100, 19);
 
         cp5.addButton("Detener")
                 .setValue(100)
                 .setPosition(10, 570)
                 .setSize(100, 19);
 
         cp5.addButton("Defecto")
                 .setPosition(10, 590)
                 .setSize(100, 19)
                 .setValue(0);
     }
 
     void changeIcon() {
         ImageIcon titlebaricon = new ImageIcon(loadBytes("/imagenes/iconClock.png"));
         frame.setIconImage(titlebaricon.getImage());
         frame.setTitle("TimePower");
     }
 
     void changePointer() {
         cursor(iconCursor);
     }
 
     void loadGraphics() {
         iconCursor = loadImage("/imagenes/cursor.png");
         TType1 = loadImage("/imagenes/moto.png");
         TType2 = loadImage("/imagenes/amarillo.png");
         TType3 = loadImage("/imagenes/verde.png");
         TType4 = loadImage("/imagenes/rojo.png");
         OType1 = loadImage("/imagenes/cow1.png");
         OType2 = loadImage("/imagenes/cow2.png");
         SPType1 = loadImage("/imagenes/servicePointGreen.png");
         SPType2 = loadImage("/imagenes/servicePointYellow.png");
         SPType3 = loadImage("/imagenes/servicePointRed.png");
     }
 
     void optionCreate() {
         // group number 1, contains 2 bangs
         Group g1 = cp5.addGroup("myGroup1")
                 .setBackgroundColor(color(0, 64))
                 .setBackgroundHeight(150);
 
 
         cp5.addBang("bang")
                 .setPosition(10, 20)
                 .setSize(100, 100)
                 .moveTo(g1)
                 .plugTo(this, "shuffle");
         ;
 
         // group number 2, contains a radiobutton
         Group g2 = cp5.addGroup("Obstaculos")
                 .setBackgroundColor(color(0, 64))
                 .setBackgroundHeight(95);
 
         cp5.addRadioButton("radio")
                 .setPosition(10, 20)
                 .setItemWidth(20)
                 .setItemHeight(20)
                 .addItem("Tipo1", 0)
                 .addItem("Tipo2", 1)
                 .setColorLabel(color(255))
                 .activate(2)
                 .moveTo(g2);
 
         // group number 3, contains a bang and a slider
         Group g3 = cp5.addGroup("myGroup3")
                 .setBackgroundColor(color(0, 64))
                 .setBackgroundHeight(150);
 
         cp5.addBang("shuffle")
                 .setPosition(50, 80)
                 .setSize(40, 50)
                 .moveTo(g3);
 
         cp5.addSlider("hello")
                 .setPosition(15, 20)
                 .setSize(100, 20)
                 .setRange(100, 500)
                 .setValue(100)
                 .moveTo(g3);
 
         cp5.addSlider("world")
                 .setPosition(15, 50)
                 .setSize(100, 20)
                 .setRange(100, 500)
                 .setValue(200)
                 .moveTo(g3);
 
         // create a new accordion
         // add g1, g2, and g3 to the accordion.
         accordion = cp5.addAccordion("acc")
                 .setPosition(5, 20)
                 .setWidth(150)
                 .addItem(g1)
                 .addItem(g2)
                 .addItem(g3);
 
         cp5.mapKeyFor(new ControlKey() {
             public void keyEvent() {
                 accordion.open(0, 1, 2);
             }
         }, 'o');
         cp5.mapKeyFor(new ControlKey() {
             public void keyEvent() {
                 accordion.close(0, 1, 2);
             }
         }, 'c');
         cp5.mapKeyFor(new ControlKey() {
             public void keyEvent() {
                 accordion.setWidth(100);
             }
         }, '1');
         cp5.mapKeyFor(new ControlKey() {
             public void keyEvent() {
                 accordion.setPosition(0, 0);
                 accordion.setItemHeight(190);
             }
         }, '2');
         cp5.mapKeyFor(new ControlKey() {
             public void keyEvent() {
                 accordion.setCollapseMode(ControlP5.ALL);
             }
         }, '3');
         cp5.mapKeyFor(new ControlKey() {
             public void keyEvent() {
                 accordion.setCollapseMode(ControlP5.SINGLE);
             }
         }, '4');
         cp5.mapKeyFor(new ControlKey() {
             public void keyEvent() {
                 cp5.remove("myGroup1");
             }
         }, '0');
 
         accordion.open(0, 1, 2);
 
         // use Accordion.MULTI to allow multiple group 
         // to be open at a time.
         accordion.setCollapseMode(Accordion.MULTI);
 
         // when in SINGLE mode, only 1 accordion  
         // group can be open at a time.  
         // accordion.setCollapseMode(Accordion.SINGLE);
     }
 
     public void Iniciar(int theValue) {
         setUpEnviroment();
        pause = false;
     }
 
     public void Detener(int theValue) {
         BusinessSimulation.getInstance().reset();
         pause = true;
         flag = false;
     }
 
     public void Defecto(int theValue) {
         setUpEnviroment();
         flag = true;
     }
 
     public void radio(int theC) {
         switch (theC) {
             case (0):
                 TYPE_OBSTRUCTION = 1;
                 break;
             case (1):
                 TYPE_OBSTRUCTION = 2;
                 break;
         }
     }
 
     public void shuffle() {
         c = color(random(255), random(255), random(255), random(128, 255));
     }
 
     public void initMap() {
 
         fill(lerpColor(color(0, 150, 150), color(0, 150, 150), 80));
         rect(0, 0, PanelWidth, PanelHeight);
         stroke(200);
 
         for (int i = PanelWidth; i <= WindowWidth - ClearanceWidth; i++) {
             if (i % ModuleMappedWidth == 0) {
                 line(i, 0, i, WindowHeight);
             }
         }
         for (int j = 0; j <= WindowHeight; j++) {
             if (j % ModuleMappedHeight == 0) {
                 line(PanelWidth, j, WindowWidth - ClearanceWidth, j);
             }
         }
     }
 
     public void drawTransport() {
         for (Transport u : BusinessSimulation.getInstance().getTransports()) {
             switch (u.getType()) {
                 case 1:
                     TYPE_TRANSPORT = "tipo1";
                     break;
                 case 2:
                     TYPE_TRANSPORT = "tipo2";
                     break;
                 case 3:
                     TYPE_TRANSPORT = "tipo3";
                     break;
                 case 4:
                     TYPE_TRANSPORT = "tipo4";
                     break;
             }
            loadTransportGraphic((u.getPosX()) * ModuleMappedWidth + PanelWidth, (u.getPosY()) * ModuleMappedHeight);
         }
     }
 
     public void loadTransportGraphic(int coordX, int coordY) {
         switch (TYPE_TRANSPORT) {
             case "tipo1":
                 image(TType1, coordX, coordY, 40, 40);
                 break;
             case "tipo2":
                 image(TType2, coordX, coordY, 40, 40);
                 break;
             case "tipo3":
                 image(TType3, coordX, coordY, 40, 40);
                 break;
             case "tipo4":
                 image(TType4, coordX, coordY, 40, 40);
                 break;
         }
     }
 
     public void drawObstruction() {
         for (Obstruction u : BusinessSimulation.getInstance().getObstructions()) {
             switch (u.getType()) {
                 case 1:
                     TYPE_OBSTRUCTION = 1;
                     break;
                 case 2:
                     TYPE_OBSTRUCTION = 2;
                     break;
             }
            loadObstructionGraphic(((u.getPosX()) * ModuleMappedWidth) + PanelWidth, (u.getPosY()) * ModuleMappedHeight);
         }
     }
 
     public void loadObstructionGraphic(int coordX, int coordY) {
         switch (TYPE_OBSTRUCTION) {
             case 1:
                 image(OType1, coordX, coordY, 40, 40);
                 break;
             case 2:
                 image(OType2, coordX, coordY, 40, 40);
                 break;
         }
     }
 
     public void drawServicePoint() {
         for (ServicePoint u : BusinessSimulation.getInstance().getServicePoints()) {
             switch (u.getType()) {
                 case 1:
                     TYPE_SERVICEPOINT = "tipo1";
                     break;
                 case 2:
                     TYPE_SERVICEPOINT = "tipo2";
                     break;
                 case 3:
                     TYPE_SERVICEPOINT = "tipo3";
                     break;
             }
            loadServicePointGraphic((u.getPosX()) * ModuleMappedWidth + PanelWidth, (u.getPosY()) * ModuleMappedHeight);
         }
 
     }
 
     public void loadServicePointGraphic(int coordX, int coordY) {
         switch (TYPE_SERVICEPOINT) {
             case "tipo1":
                 image(SPType1, coordX, coordY, 40, 40);
                 break;
             case "tipo2":
                 image(SPType2, coordX, coordY, 40, 40);
                 break;
             case "tipo3":
                 image(SPType3, coordX, coordY, 40, 40);
                 break;
         }
     }
 
     private void setUpEnviroment() {
         //orden de los parametros: PacksInStock, posX, posY, Type
         BusinessSimulation.getInstance().newServicePoint(3, 5, 3, 1);
         BusinessSimulation.getInstance().newServicePoint(9, 1, 17, 1);
         BusinessSimulation.getInstance().newServicePoint(8, 17, 15, 1);
         BusinessSimulation.getInstance().newServicePoint(2, 21, 2, 1);
         BusinessSimulation.getInstance().newServicePoint(8, 18, 5, 1);
         BusinessSimulation.getInstance().newServicePoint(7, 7, 10, 1);
     }
 
     public void mouseClicked() {
         if (mouseButton == RIGHT) {
            if (((mouseX - PanelWidth) / ModuleMappedWidth) >= 0 && ((mouseX - PanelWidth) / ModuleMappedWidth) <= 23
                    && (mouseY / ModuleMappedHeight) >= 0 && (mouseY / ModuleMappedHeight) <= 17) {
                BusinessSimulation.getInstance().newObstruction(((mouseX - PanelWidth) / ModuleMappedWidth),
                        (mouseY / ModuleMappedHeight), TYPE_OBSTRUCTION, 1);
            }

         }
     }
 }
