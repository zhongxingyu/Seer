 package com.ai;
 import java.io.IOException;
 import java.util.Vector;
 import java.util.Hashtable;
 import org.ksoap2.SoapEnvelope;
 import org.ksoap2.serialization.SoapObject;
 import org.ksoap2.serialization.SoapSerializationEnvelope;
 import org.ksoap2.transport.HttpTransport;
 import org.xmlpull.v1.XmlPullParserException;
 
 
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import net.rim.device.api.ui.*;
 import net.rim.device.api.ui.component.*;
 import net.rim.device.api.ui.container.*;
 import net.rim.device.api.system.*;
 
 public class salesControllerScreen extends MainScreen 
 {
     public salesControllerScreen() 
     {
         super();
         this.paintScreen();
         new RefreshScreenTask(this);
     }
     private void clearScreen()
     {
     	try
     	{
     		this.deleteAll();
     	}
     	catch(Exception ex)
     	{
     		this.add(new RichTextField(ex.getMessage()));
     	}
     }
     private void paintScreen()
     {
         salesmonitormodel model= new salesmonitormodel();
         Hashtable params= new Hashtable();
         int countParams=0;
         //add(new RichTextField("Ventas: " + this.adquireData()));
         try
         {
         	
         	parameter param=new parameter("id", new Integer(1));
         	++countParams;
         	params.put(new Integer(countParams), param);
         	Vector result=model.adquireData("ventas_hoy", "http://tempuri.org#ventas_hoy", params);
         	if(result.size()>0)
         	{
         		String hora=((SoapObject) result.elementAt(0)).getProperty(2).toString();
                 LabelField title = new LabelField("Ventas de la Latina a las " + hora, LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
                 setTitle(title);
         		
 	        	for(int i=0; i<result.size();i++)
 	        	{
 	        		SoapObject data=(SoapObject) result.elementAt(i);  
 	        		String Linea="Tienda: "+data.getProperty(0)+" Fecha: "+data.getProperty(1)+" Hora: "+data.getProperty(2)+" Venta: "+data.getProperty(3);
 	        		RichTextField rf=new RichTextField(Linea);
 	        		Font font = Font.getDefault();
 	        		font.derive(Font.PLAIN,2);
 	        		rf.setFont(font);
 	        		this.add(rf);        		
 	        		//this.add(new labelhyperlink("Detalles", Integer.parseInt(data.getProperty(4).toString())));
	        		this.add(new labelhyperlink("Detalles", Integer.parseInt(data.getProperty(4).toString()),Float.valueOf(data.getProperty(3).toString().replace(',', '.')).floatValue(),this));
 	        		
 	        	}
         	}
         	else
         		this.add(new RichTextField("No se devolvio informacin"));
         }        
         catch(Exception ex)
         {
         	this.add(new RichTextField(ex.getMessage()));
         }
         /*for (int i=0; i<4;i++)
         {
         add(new RichTextField("Tienda: " + new Integer(i).toString() ));
         add(new RichTextField("		Ventas: " + new Integer(1000+i).toString() + " Detalles" ));
         }*/
     	
     }
     public void repaintScreen()
     {
     	this.clearScreen();
     	this.paintScreen();
     	//this.add(new RichTextField("Repintado"));
     }
     	
     class RefreshScreenTask extends TimerTask
     {
     		private Timer timer;
     		salesControllerScreen SC;
     		public RefreshScreenTask(salesControllerScreen sc)
     		{
     			SC=sc;
     			timer=new Timer();
     			timer.schedule(this,3600000,3600000);
     			
     		}
     		public void run ()
     		{ 
     		 synchronized (Application.getEventLock()) 
     		{
     			 SC.repaintScreen();	
 			}
     		     		 
     		//timer.cancel (); 
     		}
     }
         
     
 }
