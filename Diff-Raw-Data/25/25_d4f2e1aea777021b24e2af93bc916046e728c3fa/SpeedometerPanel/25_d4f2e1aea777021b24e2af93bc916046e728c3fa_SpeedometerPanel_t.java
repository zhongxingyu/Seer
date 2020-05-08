 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * SpeedometerPanel.java
  *
  * Created on May 13, 2010, 11:33:12 PM
  */
 package speedometer;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import javax.swing.Timer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.imageio.ImageIO;
 /**
  *
  * @author Alexandru Popescu
  */
 public class SpeedometerPanel extends javax.swing.JPanel  implements KeyListener {
     public double x0=450;                                       //coordonata x baza ac vitezometru
     public double x0r=170;                                      //coordonata x baza ac turometru
     public double y0=191;                                       //coordonata y baza ac vitezometru
     public double y0r=191;                                      //coordonata y baza ac turometru
     public double x0f=260;                                      //coordonata x baza ac indicator combustibil
     public double y0f=215;                                      //coordonata x baza ac indicator combustibil
     public double xf=223;                                       //coordonata y varf ac indicator combustibil
     public double yf=200;                                       //coordonata y varf ac indicator combustibil
     public double x=354;                                        //coordonata x varf ac vitezometru
     public double xr=64;                                        //coordonata x varf  ac turometru
     public double y=328+21-45;                                  //coordonata y varf ac vitezometru
     public double yr=339-45;                                    //coordonata y varf ac turometru
     public double v=0;                                          //valoarea vitezei
     public double turatie=0;                                    //valoarea turatiei
     public double comparturatie=0,comparviteza=0;               //folosite sa verifice daca viteza creste sau scade in actionlistener
     public boolean s=true,d=false;                              //folosite sa vada in ce zona e acul vitezometrului(schimba formula)
     public double pas1=1;                                       //pas pt accelerare vitezometru
     public double pas2=1;                                       //pas pt decelerare vitezometru
     public double pas3=1;                                       //pas pt accelerare turometru
     public double pas4=3;                                       //pas pt decelerare turometru
     public int prag1=0;                                         //Prag pentru actualizarea turatiei (declanseaza trecerea in viteza 2)
     public int prag2=0;                                         //Prag pentru actualizarea turatiei (declanseaza trecerea in viteza 3)
     public int prag3=0;                                         //Prag pentru actualizarea turatiei (declanseaza trecerea in viteza 4)
     public int prag4=0;                                         //Prag pentru actualizarea turatiei (declanseaza trecerea in viteza 5)
     public int prag5=0;                                         //Prag pentru actualizarea turatiei (declanseaza trecerea in viteza 6)
     public int viteza=0;                                        //Treapta de viteza
     public int zona1=1;                                         //zona de jos pt vitezometru 
     public int zona2=0;                                         //zona de sus pt vitezometru
     public int zona3=1;                                         //zona de jos pentru turometru 
     public int zona4=0;                                         //zona de sus pentru turometru
     public boolean apasatoprit=false;                           //determina daca se apasa butonul rosu "Stop Engine"
     public boolean apasatpornit=false;                          //determina daca se apasa butonul verde "Start Engine"
     public boolean control=true;                                //determina daca se tine cont de apasarea tastelor (e setat in AppWindow)
     public boolean sunet=true;                                  //determina daca se aude sunetul motorului (e setat in AppWindow)
     public boolean pornit=false;                                //determina daca e pornit motorul
     public boolean alarmaBaterie=false;                         //determina daca s-a activat alarma de baterie
     public boolean alarmaPompa=false;                           //determina daca s-a activat alarma de pompa
     public boolean alarmaUlei=false;                            //determina daca s-a activat alarma de ulei
     public boolean alarmaUsi=false;                             //determina daca s-a activat alarma de usi
     public boolean alarmaCentura;                               //determina daca s-a activat alarma de centura
     Icon butonverde;                                            //imagine buton
     Icon butonrosu;                                             //imagine buton
     Icon baterierosie;                                          //imagine atentionare
     Icon bateriegri;                                            //imagine atentionare
     Icon pompagri;                                              //imagine atentionare
     Icon pomparosie;                                            //imagine atentionare
     Icon uleigri;                                               //imagine atentionare
     Icon uleirosie;                                             //imagine atentionare
     Icon usigri;                                                //imagine atentionare
     Icon usirosie;                                              //imagine atentionare
     Icon centuragri;                                            //imagine atentionare
     Icon centurarosie;                                          //imagine atentionare
     public boolean crescutturatie=false;                        //determina daca la pornire turatia s-a dus la 1000
     public boolean idle=false;                                  //determina daca acul e la 1000
     public boolean cresteturatie=false;                         //determina daca dupa calcularea unei noi turatii aceasta a crescut(folosita la sunet)
     public boolean cresteviteza=false;                          //determina daca dupa calcularea unei noi viteze aceasta a crescut(folosita la sunet)
     SensorAlarm alarm = new SensorAlarm();                      //instanta clasei SensorAlarm (folosit pt metodele de sunet)
     public boolean frana=false;                                 //determina daca s-a apasat tasta de jos (pt declansarea sunetului de franare)
     public boolean sunetacceleratiemaxima=false;                //folosit in sunet() pt declansarea sunetului de acceleratie maxima (turatie>6500)
     public boolean sunetacceleratieoprita=false;                //folosit in sunet() pt declansarea sunetului oprire a accelerarii 
     public boolean sunetacceleratie=false;                      //folosit pt declansarea sunetului de accelerare
     public boolean sunetmotor=false;                            //folosit in sunet() pentru declansarea sunetului de motor
     BufferedImage buffer;                                       //imagine folosita la double buffering
     Graphics2D gbuffer;                                         //obiect grafic folosit pt desenare in buffer
     public boolean firstTime=true;                              //determina daca s-a initializat sau nu imaginea si obiectul grafic pentru db
     Rectangle area;                                             //folosit la crearea imaginii folosita la buffering
     Image m;                                                    //in ea se retine imaginea de background
 
     public SpeedometerPanel()throws IOException, SQLException  {//Constructor
         initComponents();
         addKeyListener(this);                                   //adauga keylistener la panou
         t.start();                                              //porneste timerul
         try {                                                   //copiaza in m imaginea de background
             m =ImageIO.read(new File("src/images/Speedometer.jpg"));
         } catch (IOException ex) {
             Logger.getLogger(SpeedometerPanel.class.getName()).log(Level.SEVERE, null, ex);
         }
         butonverde = new ImageIcon("src/images/buttongreen.jpg");
         butonrosu = new ImageIcon("src/images/buttonred.jpg");
         baterierosie=new ImageIcon("src/images/battery_red.jpg");
         bateriegri=new ImageIcon("src/images/battery_gray.jpg");
         pompagri=new ImageIcon("src/images/pump_gray.jpg");
         pomparosie=new ImageIcon("src/images/pump_red.jpg");
         uleigri=new ImageIcon("src/images/oil_gray.jpg");
         uleirosie=new ImageIcon("src/images/oil_red.jpg");
         usigri=new ImageIcon("src/images/doors_gray.jpg");
         usirosie=new ImageIcon("src/images/doors_red.jpg");
         centuragri=new ImageIcon("src/images/seatbelt_gray.jpg");
         centurarosie=new ImageIcon("src/images/seatbelt_red.jpg");
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     ActionListener actionListener = new ActionListener() {
         
         public void actionPerformed(ActionEvent actionEvent) {
//            System.out.print(turatie+" ");
//            System.out.print(zona3+" ");
//            System.out.print(zona4+" ");
//            System.out.println("xr= "+(int)xr);

             if ((crescutturatie==false)&&(pornit==true)&&(turatie<1000))
                 cresteTuratieLaPornire();                       //duce acul turometrului la 1000 la pornire
             setarePasi();                                       //seteaza pasii de accelerare/decelerare in functie de viteza
             calculViteza();                                     //calculeaza viteza
             calculTuratie();                                    //calculeaza turatia
             if (sunet)                                          //daca nu e selectat mute sa se aplice metoda de sunet
                 sunet();
             if (apasatpornit)                                   //daca s-a apasat butonul de start si era pe mute, sa nu repete zgomotul de pornire motor la unmute
                 apasatpornit=false;
             actualizareTuratie();                               //actualizeaza turatia in functie de praguri (pt schimbarea de viteze)
             decelerareV();                                      //metoda ce simuleaza decelerarea pentru acul vitezometrului
             decelerareF();
             if ((crescutturatie)||(pornit==false))
                 decelerareR();                                  //metoda ce simuleaza decelerarea pentru acul turometrului
             jLabel2.setText(Integer.toString((int)v));
             double v2=v*1.6;
             jLabel3.setText(Integer.toString((int)v2));
             setareTreapta();
             jLabel7.setText(Integer.toString(viteza));
             jLabel8.setText(Integer.toString((int)turatie));
             if (crescutturatie){                                //daca motorul a fost pornit si turatia a ajuns
                 try {                                           //la 1000, se introduce o intarziere de 100ms
                             Thread.sleep(100);
                         } catch (InterruptedException ex) {
                             Logger.getLogger(SpeedometerPanel.class.getName()).log(Level.SEVERE, null, ex);
                         }
             }
         }
     };
 
     Timer t=new Timer(10,actionListener);
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     public void sunet(){
         if (apasatpornit){                                      //Da drumu la sunetu de start de motor si la cel de oprire motor
             alarm.StartEngine();
             apasatpornit=false;
         }
         if (apasatoprit){
             alarm.ThrottleStop();
             apasatoprit=false;
         }        
         if (pornit && (sunetmotor==false)){                     //Da drumu la sunetul de motor pornit idle
             alarm.EngineNoise();
             sunetmotor=true;
         }
         if (!pornit && sunetmotor){                             //Da drumu la sunetul de oprire a motorului
             alarm.StopEngineNoise();
             sunetmotor=false;
         }
         if ((turatie>6650)&&(sunetacceleratiemaxima==false)){    //Da drumu la sunetul de acceleratie maxima
             alarm.StartMaxThrottle();
             sunetacceleratiemaxima=true;
         }
         if ((turatie<6650)&&sunetacceleratiemaxima){            //Opreste sunetul de acceleratie maxima
             alarm.StopMaxThrottle();
             sunetacceleratiemaxima=false;
         }
         if (cresteturatie&&crescutturatie){                     
             if ((sunetacceleratie==false)&&pornit){
                 alarm.StopThrottleStop();
                 alarm.StartThrottle();
                 sunetacceleratieoprita=true;
                 alarm.StopEngineNoise();
                 sunetmotor=false;
                 sunetacceleratie=true;
                 frana=false;
             }
         }
         else{
             if (pornit){
                 alarm.StopThrottle();
                 if (sunetacceleratieoprita && (turatie<6650)){
                     alarm.ThrottleStop();
                     sunetacceleratieoprita=false;
                 }
                 sunetacceleratie=false;
             }
         }
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     public void setareTreapta(){
         if (pornit==false)
             viteza=0;
         else{
             if ((prag1==1)&&(prag2==0)&&(prag3==0)&&(prag4==0)&&(prag5==0))
                 viteza=2;
             else
                 if((prag1==1)&&(prag2==1)&&(prag3==0)&&(prag4==0)&&(prag5==0))
                     viteza=3;
                 else
                     if((prag1==1)&&(prag2==1)&&(prag3==1)&&(prag4==0)&&(prag5==0))
                         viteza=4;
                     else
                         if((prag1==1)&&(prag2==1)&&(prag3==1)&&(prag4==1)&&(prag5==0))
                             viteza=5;
                         else
                             if((prag1==1)&&(prag2==1)&&(prag3==1)&&(prag4==1)&&(prag5==1))
                             viteza=6;
                             else
                                 viteza=1;
             }
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     public void calculViteza(){
         comparviteza=v;
         if(((int)x>437)&&(s==true)){
                 d=true;
                 s=false;
             }
         else
             if (((int)x<437)&&(s==false)){
                 d=false;
                 s=true;
             }
         if (y<90+21-45)
             v=0.00000146245*Math.pow( x,3)-0.00197303*Math.pow(x,2)+1.07994*x-146.124;
         else
             if ((s==true)&&(y>90+21-45))
                 v=-0.000003916*Math.pow(y,3)+0.002106564*Math.pow(y,2)- 0.562179848*y+86.798186209;
             else
                 v=0.000002682*Math.pow(y,3)- 0.001523562*Math.pow(y,2)+0.473605642*y+65.175332050;
         if ((x==354)&&(zona1==1))
             v=0;
         if (v<0)
           v=0;
          if (comparviteza<v)
             cresteviteza=true;
         else
             cresteviteza=false;
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     public void calculTuratie(){
         comparturatie=turatie;
        if (yr<91 && xr>110){                                     //Daca e in zona de turatie 4000+
            System.out.print("prima formula  ");
             turatie=0.000305*Math.pow(xr,3)-0.165550*Math.pow(xr,2)+43.551051*xr+1125.570795;
        }
        else{                                                    //Daca e in zona de turatie 4000-
             turatie=-0.000289881*Math.pow(yr,3)+0.151079520*Math.pow(yr,2)- 40.076318640*yr+6091.969107107 ;
             System.out.print("a doua formula  ");
        }
         if (comparturatie<turatie)
             cresteturatie=true;
         else
             cresteturatie=false;
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     public void setareZoneV(){
         if((zona2==1)&&((int)y>=185+21-45)){                    //Scoate din zona2
             zona2=0;
         }
         if ((zona1==1)&&((int)y<=240+21-45)){                   //Scoate din zona1
             zona1=0;
         }
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     public void setareZoneR(){
         if ((zona4==1)&&((int)yr>=185+21-45)&&(yr!=210+21-45)){ //Scoate din zona4
             zona4=0;
         }
         else
             if ((zona3==1)&&((int)yr<=245-45)&&(yr!=210-45)) {  //Scoate in zona 3
                 zona3=0;
             }
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     public void calculCoordonateUV(){
         if ((x<=304)&&(zona1==0)&&(zona2==0)){                  //Daca intra in zona mijloc stanga vitezometru
                 y=y-3;
                 if (y<185+21-45){
                     zona2=1;
                     x=306;
                     y=(-1)*Math.sqrt(Math.abs(22050-Math.pow((x-x0),2)))+y0;
                 }
         }
         else
             if ((x>=590)&&(zona2==0)&&(zona1==0)){              //Daca intra in zona mijloc dreapta vitezometru
                 y=y+2;
                 if (y>240+21-45){
                     zona1=1;
                     x=595;
                     y=Math.sqrt(Math.abs(22050-Math.pow((x-x0),2)))+y0;
                 }
             }
             else
                 if(zona1==1){                                   //Daca e in zona jos vitezometru
                     x=x-pas1;
                     y=Math.sqrt(Math.abs(22050-Math.pow((x-x0),2)))+y0;
                 }
                 else
                     if (zona2==1){                              //Daca e in zona sus vitezometru
                         x=x+pas1;
                         y=(-1)*Math.sqrt(Math.abs(22050-Math.pow((x-x0),2)))+y0;
                     }
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     public void calculCoordonateDV(){
         if (x<=304){                                            //Daca intra in zona mijloc stanga vitezometru
                 y=y+5;
                 if (y>245+21-45){
                     zona1=1;
                     x=306;
                     y=Math.sqrt(Math.abs(22050-Math.pow((x-x0),2)))+y0;
                 }
         }
         else
             if (x>=596){                                        //Daca intra in zona mijloc dreapta vitezometru
                 y=y-2;
                 if (y<185+21-45){
                     zona2=1;
                     x=595;
                     y=(-1)*Math.sqrt(Math.abs(22050-Math.pow((x-x0),2)))+y0;
                 }
             }
             else
                 if(zona1==1){                                   //Daca e in zona jos vitezometru
                     x=x+pas1;
                     y=Math.sqrt(Math.abs(22050-Math.pow((x-x0),2)))+y0;
                 }
                 else
                     if (zona2==1){                              //Daca e in zona sus vitezometru
                         x=x-pas1;
                         y=(-1)*Math.sqrt(Math.abs(22050-Math.pow((x-x0),2)))+y0;
                      }
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
      public void calculCoordonateUR(){
          if (xr<270){                                           //Sa nu depaseasca valoarea maxima de turatii
              if (yr==210+21-45)
                  zona4=1;
              if ((zona3==1)&&(xr-pas3>20)){                     //Daca e in zona jos turometru
                 xr=xr-pas3;
                 yr=Math.sqrt(Math.abs(22050-Math.pow((xr-x0r),2)))+y0r;
             }
             else
                 if ((zona4==1)&&(xr+pas3>20)){                  //Daca e in zona sus turometru
                     xr=xr+pas3;
                     yr=(-1)*Math.sqrt(Math.abs(22050-Math.pow((xr-x0r),2)))+y0r;
                 }
                 else{
                     if ((yr<200+21)&&(xr+pas3>20)){
                         xr=xr+pas3;
                         yr=(-1)*Math.sqrt(Math.abs(22050-Math.pow((xr-x0r),2)))+y0r;
                         zona4=1;
                     }
                     else
                         yr=210+21-45;
                 }
          }
      }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     public void calculCoordonateDR(){
         if (turatie>1000){                                      //Sa nu depaseasca valoarea minima de turatii
             if (yr==210+21-45)
                 zona3=1;
             if (zona3==1){                                      //Daca e in zona jos turometru
                 xr=xr+4;
                 yr=Math.sqrt(Math.abs(22050-Math.pow((xr-x0r),2)))+y0r;
             }
             else
                 if (zona4==1){                                  //Daca e in zona sus turometru
                     xr=xr-4;
                     yr=(-1)*Math.sqrt(Math.abs(22050-Math.pow((xr-x0r),2)))+y0r;
                 }
                 else{
                     if (yr>220+21){
                         xr=xr+4;
                         yr=Math.sqrt(Math.abs(22050-Math.pow((xr-x0r),2)))+y0r;
                         zona3=1;
                     }
                     else
                         yr=210+21-45;
                 }
         }
      }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     public void decelerareF(){
         if (yf<245){
             if (yf>=215){
                 xf=xf+0.001;
                 yf=Math.sqrt(Math.abs(1600-Math.pow((xf-x0f),2)))+y0f;
                 repaint();                
             }
             else{
                 xf=xf-0.001;
                 yf=(-1)*Math.sqrt(Math.abs(1600-Math.pow((xf-x0f),2)))+y0f;
                 repaint();
                 if (yf>214.5)
                     yf=215.1;
             }
         }
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     @SuppressWarnings("static-access")
     public void decelerareV(){
         if(y<328+21-45){
             if (zona1==1){                                      //Daca e in zona jos vitezometru
                 x=x+pas2;
                 y=Math.sqrt(Math.abs(22050-Math.pow((x-x0),2)))+y0;
                 repaint();
                 setareZoneV();
             }
             else
                 if (zona2==1){                                  //Daca e in zona sus vitezometru
                     x=x-pas2;
                     y=(-1)*Math.sqrt(Math.abs(22050-Math.pow((x-x0),2)))+y0;
                     repaint();
                     setareZoneV();
                 }
                 else{
                     if (v<100){                                 //Daca intra in zona mijloc stanga vitezometru
                         y=y+3;
                         if (y>245+21-45){
                             zona1=1;
                             x=306;
                         }
                     }
                     else{                                       //Daca intra in zona mijloc dreapta vitezometru
                          y=y-3;
                         if (y<185+21-45){
                             zona2=1;
                             x=595;
                         }
                     }
                     repaint();
                     setareZoneV();
                 }
         }
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     public void decelerareR(){
         if (((turatie>1000)&&pornit)||(((int)turatie>0)&&(pornit==false))){
             if ((zona3==1)&&(xr+pas4>20)){                      //Daca e in zona jos turometru
                 xr=xr+pas4;
                 yr=Math.sqrt(Math.abs(22050-Math.pow((xr-x0r),2)))+y0r;
                 repaint();
                 setareZoneR();
             }
             else                                                //Daca e in zona sus turometru
                 if ((zona4==1)&&(xr+pas4>20)){
                     xr=xr-pas4;
                     yr=(-1)*Math.sqrt(Math.abs(22050-Math.pow((xr-x0r),2)))+y0r;
                     repaint();
                     setareZoneR();
                 }
                 else{
                     xr=xr+pas4;
                     yr=Math.sqrt(Math.abs(22050-Math.pow((xr-x0r),2)))+y0r;
                     if (yr>220+21-45)
                         zona3=1;
                 }
         }
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     public void setarePasi(){
         if ((int)v<60)                                          //Setare pasi vitezometru
             pas1=2;
          else
             if ((int)v<90){
                 pas1=1;
                 pas2=0.5;
             }
             else{
                 pas1=0.5;
                 pas2=0.35;
             }
         if (turatie<3000)                                       //Setare pas crestere turatii functie de valoarea turatiei
             pas3=5;
         if (turatie>3000)
             pas3=4;
         if (turatie>4000){
             pas3=3;
             pas4=2;
         }
         if (turatie>5000){
             pas3=2;
             pas4=1;
         }
         if (turatie>5500){
             pas3=1.5;
             pas4=0.75;
         }
         if (turatie>6000)
             pas3=1;
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     public void actualizareTuratie(){                           //Actualizam pragurile de turatie
         if ((prag1==0)&&(turatie>5000)){                        //Ducem turatia la 2535 daca depaseste prima
             prag1=1;                                            // oara 5000 (aplicare prag 1)
             xr=28;
             yr=147;
             zona3=0;
             zona4=1;
             turatie=2535;
             alarm.StopThrottle();
             sunetacceleratie=false;
         }
         if ((prag2==0)&&(turatie>5500)){                        //Ducem turatia la 2535 daca depaseste prima
             prag2=1;                                            // oara 5500 (aplicare prag 2)
             xr=28;
             yr=147;
             zona3=0;
             zona4=1;
             turatie=2535;
             alarm.StopThrottle();
             sunetacceleratie=false;
         }
         if ((prag3==0)&&(turatie>6000)){                        //Ducem turatia la 3056 daca depaseste prima
             prag3=1;                                            // oara 6000 (aplicare prag 3)
             xr=76;
             yr=76;
             zona3=0;
             zona4=1;
             turatie=3790;
             alarm.StopThrottle();
             sunetacceleratie=false;
         }
         if ((prag4==0)&&(turatie>6300)){                        //Ducem turatia la 4769 daca depaseste prima
             prag4=1;                                            // oara 6300 (aplicare prag 4)
             xr=137;
             yr=46;
             zona3=0;
             zona4=1;
             turatie=4769;
             alarm.StopThrottle();
             sunetacceleratie=false;
         }
         if ((prag5==0)&&(turatie>6500)){                        //Ducem turatia la 5034 daca depaseste prima
             prag5=1;                                            // oara 6500 (aplicare prag 5)
             xr=155;
             yr=43;
             zona3=0;
             zona4=1;
             turatie=5034;
             alarm.StopThrottle();
             sunetacceleratie=false;
         }
          if ((prag5==1)&&(v<100))                               //Reseteaza prag5 daca viteza scade sub 100Mph
             prag5=0;
         if ((prag4==1)&&(v<90))                                 //Reseteaza prag4 daca viteza scade sub 90Mph
             prag4=0;
         if ((prag3==1)&&(v<70))                                 //Reseteaza prag3 daca viteza scade sub 70Mph
             prag3=0;
         if ((prag2==1)&&(v<40))                                 //Reseteaza prag2 daca viteza scade sub 40Mph
             prag2=0;
         if ((prag1==1)&&(v<15))                                 //Reseteaza prag1 daca viteza scade sub 20Mph
             prag1=0;
         if ((turatie>=1000)&&pornit)
             crescutturatie=true;
         if ((crescutturatie==true)&&(turatie<1000)&&(pornit==true))
             turatie=1000;
         if ((pornit==false)&&(turatie<0))
             turatie=0;
         if (pornit &&(turatie<=1000)&&crescutturatie&&(idle==false)){
             xr=34;
             yr=274+21-45;
             repaint();
             idle=true;
         }
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     @SuppressWarnings("static-access")
     public void cresteTuratieLaPornire(){                       //Duce acul turometrului la 100 la apasare start
             xr=xr-5;
             yr=Math.sqrt(Math.abs(22050-Math.pow((xr-x0r),2)))+y0r;
             try {
                     Thread.sleep(50);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(SpeedometerPanel.class.getName()).log(Level.SEVERE, null, ex);
                 }
             repaint();
             setareZoneR();
         if (turatie>=1000)
             crescutturatie=true;
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     @Override
     public void paintComponent(Graphics g){
         if (firstTime) {                                        //Initializare imagine buffer
           Dimension dim = getSize();
           int w = dim.width;
           int h = dim.height;
           area = new Rectangle(dim);
           buffer = (BufferedImage) createImage(w, h);
           gbuffer = buffer.createGraphics();
           gbuffer.setColor(Color.red);
           gbuffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
           firstTime = false;
         }
         Graphics2D g2=(Graphics2D) gbuffer;
         g2.clearRect(0, 0, area.width, area.height);
         g2.drawImage(m, 0,0, this);                             //Deseneaza imaginea de fundal in buffer
         g2.setColor(Color.orange);                              //Setari pt obiectul grafic
 	g2.setStroke(new BasicStroke(4));
 	g2.drawLine((int)x0, (int)y0, (int)x, (int)y);          //Desenare ac vitezometru in buffer
         g2.drawLine((int)x0-1, (int)y0-1, (int)x, (int)y);
         g2.drawLine((int)x0+1, (int)y0+1, (int)x, (int)y);
         g2.drawLine((int)x0+1, (int)y0-1, (int)x, (int)y);
         g2.drawLine((int)x0-1, (int)y0+1, (int)x, (int)y);
         g2.drawLine((int)x0r, (int)y0r, (int)xr, (int)yr);      //Desenare ac turometru in buffer
         g2.drawLine((int)x0r-1, (int)y0r-1, (int)xr, (int)yr);
         g2.drawLine((int)x0r+1, (int)y0r+1, (int)xr, (int)yr);
         g2.drawLine((int)x0r+1, (int)y0r-1, (int)xr, (int)yr);
         g2.drawLine((int)x0r-1, (int)y0r+1, (int)xr, (int)yr);
         g2.setColor(Color.black);                               //Desenare cercuri negre
         g2.fillOval(435, 177, 30, 30);
         g2.fillOval(155, 177, 30, 30);
         
         g2.setColor(Color.orange);
         g2.setStroke(new BasicStroke(3));
         g2.drawLine((int)x0f, (int)y0f, (int)xf, (int)yf);
         g2.drawLine((int)x0f-1, (int)y0f-1, (int)xf, (int)yf);
         g2.drawLine((int)x0f+1, (int)y0f+1, (int)xf, (int)yf);
         g2.drawLine((int)x0f+1, (int)y0f-1, (int)xf, (int)yf);
         g2.drawLine((int)x0f-1, (int)y0f+1, (int)xf, (int)yf);
         
         g.drawImage(buffer, 0, 0, this);                        //Deseneaza imaginea buffer
 //        System.out.print("  s=");System.out.print(s);
 //        System.out.print("  x=");System.out.print(x);         //Afisare coordonate varf ace
 //        System.out.print("  y=");System.out.println((int)y);
 //        System.out.print(" turatie=");System.out.print(turatie);
 //        System.out.print(" viteza=");System.out.print((int)v);
 //        System.out.print("  xr=");System.out.print(xr);
 //        System.out.print("  yr=");System.out.println((int)yr);
 //        System.out.print("  z1=");System.out.print(zona1);    //Afisare zone
 //        System.out.print("  z2=");System.out.println(zona2);
 //        System.out.print("  z3=");System.out.print(zona3);
 //        System.out.print("  z4=");System.out.println(zona4);
     }
 //-----------------------------------------------------------------------------------
     public void keyTyped(KeyEvent e) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
      public void keyPressed(KeyEvent e) {                       //Daca se apasa o tasta
          int tasta;
          tasta=e.getKeyCode();         
          if (control){
              if (tasta==KeyEvent.VK_DOWN){                      //Daca se apasa sageata jos
                 if (v>0){
                    if (frana==false){                        //Zgomot de franare
                        alarm.Brake();
                        frana=true;
                    }
                     calculCoordonateDV();
                     setareZoneV();
                 }
                 if (turatie>1000){
                     calculCoordonateDR();
                     setareZoneR();
                     repaint();
                 }
             }
             if (tasta==KeyEvent.VK_UP){                         //Daca se apasa sageata sus
                 idle=false;
                 if (pornit&&(crescutturatie)){
                         if (v<140){
                         calculCoordonateUV();
                         setareZoneV();
                     }
                     calculCoordonateUR();
                     setareZoneR();
                     repaint();
                 }
             }
         }
     }
 //-----------------------------------------------------------------------------------
 //-----------------------------------------------------------------------------------
     public void keyReleased(KeyEvent e) {
     }    
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLayeredPane1 = new javax.swing.JLayeredPane();
         jLabel2 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         butonStart = new javax.swing.JLabel();
         Buton1 = new javax.swing.JLabel();
         baterie = new javax.swing.JLabel();
         pompa = new javax.swing.JLabel();
         ulei = new javax.swing.JLabel();
         usi = new javax.swing.JLabel();
         centura = new javax.swing.JLabel();
 
         setPreferredSize(new java.awt.Dimension(616, 390));
 
         jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 24));
         jLabel2.setForeground(new java.awt.Color(255, 204, 0));
         jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jLabel2.setBounds(400, 270, 60, 30);
         jLayeredPane1.add(jLabel2, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 24));
         jLabel4.setText("M/h");
         jLabel4.setBounds(450, 270, 60, 30);
         jLayeredPane1.add(jLabel4, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 24));
         jLabel3.setForeground(new java.awt.Color(255, 204, 0));
         jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jLabel3.setBounds(400, 300, 60, 30);
         jLayeredPane1.add(jLabel3, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 24));
         jLabel5.setText("Km/h");
         jLabel5.setBounds(450, 300, 60, 30);
         jLayeredPane1.add(jLabel5, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel6.setFont(new java.awt.Font("Times New Roman", 1, 24));
         jLabel6.setText("Gear");
         jLabel6.setBounds(180, 300, 60, 30);
         jLayeredPane1.add(jLabel6, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel7.setFont(new java.awt.Font("Times New Roman", 1, 24));
         jLabel7.setForeground(new java.awt.Color(255, 204, 0));
         jLabel7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jLabel7.setBounds(120, 300, 60, 30);
         jLayeredPane1.add(jLabel7, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel8.setFont(new java.awt.Font("Times New Roman", 1, 24));
         jLabel8.setForeground(new java.awt.Color(255, 204, 0));
         jLabel8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jLabel8.setBounds(120, 270, 60, 30);
         jLayeredPane1.add(jLabel8, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel9.setFont(new java.awt.Font("Times New Roman", 1, 24));
         jLabel9.setText("Revs");
         jLabel9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jLabel9.setBounds(180, 270, 60, 30);
         jLayeredPane1.add(jLabel9, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         butonStart.setFont(new java.awt.Font("Tahoma", 1, 14));
         butonStart.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         butonStart.setText("Start Engine");
         butonStart.setBounds(40, 350, 110, 30);
         jLayeredPane1.add(butonStart, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         Buton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonred.jpg"))); // NOI18N
         Buton1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 Buton1ApasareStartStop(evt);
             }
         });
         Buton1.setBounds(10, 350, 30, 30);
         jLayeredPane1.add(Buton1, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         baterie.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/battery_gray.jpg"))); // NOI18N
         baterie.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 baterieAlarmaBaterie(evt);
             }
         });
         baterie.setBounds(200, 340, 40, 39);
         jLayeredPane1.add(baterie, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         pompa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pump_gray.jpg"))); // NOI18N
         pompa.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 pompaAlarmaPompa(evt);
             }
         });
         pompa.setBounds(250, 340, 40, 40);
         jLayeredPane1.add(pompa, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         ulei.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/oil_gray.jpg"))); // NOI18N
         ulei.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 uleiAlarmaUlei(evt);
             }
         });
         ulei.setBounds(300, 340, 40, 39);
         jLayeredPane1.add(ulei, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         usi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/doors_gray.jpg"))); // NOI18N
         usi.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 usiAlarmaUsi(evt);
             }
         });
         usi.setBounds(350, 340, 40, 39);
         jLayeredPane1.add(usi, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         centura.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/seatbelt_gray.jpg"))); // NOI18N
         centura.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 centuraAlarmaCentura(evt);
             }
         });
         centura.setBounds(400, 340, 40, 40);
         jLayeredPane1.add(centura, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 616, javax.swing.GroupLayout.PREFERRED_SIZE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE)
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void Buton1ApasareStartStop(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Buton1ApasareStartStop
         if (control){
             if (pornit==false){                                 //Daca se porneste motorul
                 apasatpornit=true;
                 butonStart.setText("Stop Engine");
                 pornit=true;
                 Buton1.setIcon(butonverde);
                 viteza=1;               
             } else{                                             //Daca se opreste motorul
                 apasatoprit=true;
                 butonStart.setText("Start Engine");
                 pornit=false;
                 idle=false;
                 Buton1.setIcon(butonrosu);
                 crescutturatie=false;
             }
         }
 }//GEN-LAST:event_Buton1ApasareStartStop
                                                                 //Metode pt activarea atentionarilor
     private void baterieAlarmaBaterie(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_baterieAlarmaBaterie
         // TODO add your handling code here:
         if (alarmaBaterie){
             baterie.setIcon(bateriegri);
             alarmaBaterie=false;
         } else{
             baterie.setIcon(baterierosie);
             alarmaBaterie=true;
             alarm.BatteryAlarm();
         }
 }//GEN-LAST:event_baterieAlarmaBaterie
 
     private void pompaAlarmaPompa(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pompaAlarmaPompa
         // TODO add your handling code here:
         if (alarmaPompa){
             pompa.setIcon(pompagri);
             alarmaPompa=false;
         } else{
             pompa.setIcon(pomparosie);
             alarmaPompa=true;
             alarm.PumpAlarm();
         }
 }//GEN-LAST:event_pompaAlarmaPompa
 
     private void uleiAlarmaUlei(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_uleiAlarmaUlei
         // TODO add your handling code here:
         if (alarmaUlei){
             ulei.setIcon(uleigri);
             alarmaUlei=false;
         } else{
             ulei.setIcon(uleirosie);
             alarmaUlei=true;
             alarm.OilAlarm();
         }
 }//GEN-LAST:event_uleiAlarmaUlei
 
     private void usiAlarmaUsi(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usiAlarmaUsi
         // TODO add your handling code here:
         if (alarmaUsi){
             usi.setIcon(usigri);
             alarmaUsi=false;
         } else{
             usi.setIcon(usirosie);
             alarmaUsi=true;
             alarm.DoorsAlarm();
         }
 }//GEN-LAST:event_usiAlarmaUsi
 
     private void centuraAlarmaCentura(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_centuraAlarmaCentura
         // TODO add your handling code here:
         if (alarmaCentura){
             centura.setIcon(centuragri);
             alarmaCentura=false;
         } else{
             centura.setIcon(centurarosie);
             alarmaCentura=true;
             alarm.SeatbeltAlarm();
         }
 }//GEN-LAST:event_centuraAlarmaCentura
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel Buton1;
     private javax.swing.JLabel baterie;
     private javax.swing.JLabel butonStart;
     private javax.swing.JLabel centura;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JLayeredPane jLayeredPane1;
     private javax.swing.JLabel pompa;
     private javax.swing.JLabel ulei;
     private javax.swing.JLabel usi;
     // End of variables declaration//GEN-END:variables
 
 }
