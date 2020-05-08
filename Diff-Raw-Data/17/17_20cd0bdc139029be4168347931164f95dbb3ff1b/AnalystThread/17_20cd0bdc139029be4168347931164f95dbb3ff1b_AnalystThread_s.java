 package com.emn.fil.automaticdiscover.ihm.threads;
 
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import com.emn.fil.automaticdiscover.business.Connection;
 import com.emn.fil.automaticdiscover.business.Reseau;
 import com.emn.fil.automaticdiscover.dto.IP;
 import com.emn.fil.automaticdiscover.dto.IPRange;
 import com.emn.fil.automaticdiscover.dto.Machine;
 import com.emn.fil.automaticdiscover.dto.enums.OsType;
 import com.emn.fil.automaticdiscover.ihm.Frame;
 
 @Component
public class AnalystThread extends Thread{
 
 	@Autowired
 	Frame my_frame;
 	
 	@Autowired
 	Reseau reseau;
 	
 	@Autowired
 	Connection connection;
 	
 	
 	public void run(){
 			
 			if(IP.isValidIP(my_frame.txtbox_from.getText()) && IP.isValidIP(my_frame.txtbox_to.getText())){
 				//ON desactive le btn pendant le traitement
 				my_frame.btn_Launch.setEnabled(false);
 				
 				IP range1 = new IP(my_frame.txtbox_from.getText());
 				IP range2 = new IP(my_frame.txtbox_to.getText());
 				
 				my_frame.ta_console.append("ANALYSE LAUNCH : \r\n");
 				my_frame.ta_console.append("\tFROM :"+my_frame.txtbox_from.getText()+" \r\n");
 				my_frame.ta_console.append("\tTO :"+my_frame.txtbox_to.getText()+" \r\n");
 				my_frame.ta_console.append("\r\n");
 				
 				IPRange range = new IPRange(range1, range2);
 				OsType os_current = OsType.UNKNOWN;
 				int nb_ips = range.getTheIPRange().size();
 				System.out.println("NB IPS "+ nb_ips);
 				System.out.println(range.getTheIPRange());
 				my_frame.progressBar.setMaximum(nb_ips);
 				int nb_current = 1;
 				while (range.hasNext()) {
 					os_current = connection.getOSType(range.getCurrent());
 					my_frame.ta_console.append("IP TESTE : "+range.getCurrent().getIp());
 					if(os_current != OsType.UNKNOWN){
 						reseau.ajoutMachine(new Machine(range.getCurrent(), os_current));
 						my_frame.ta_console.append(" => AJOUTE\r\n");
 					}else
 						my_frame.ta_console.append(" => NOK\r\n");
 					
 					my_frame.progressBar.setValue(nb_current);
 					my_frame.lbl_percent.setText((nb_current*100)/nb_ips+"%");
 					nb_current++;
 					range.next();
 				}
 				
 				my_frame.ta_console.append("#############\r\n"+reseau.getResume()+"\r\n");
 				//ON reactive le btn pendant le traitement
 				my_frame.btn_Launch.setEnabled(true);
 				my_frame.lbl_percent.setText("0%");
 				my_frame.progressBar.setValue(0);
			}
			else{
 				my_frame.ta_console.append("ERREUR : IPs INCORRECTES\r\n");
 				my_frame.ta_console.append(my_frame.txtbox_from.getText()+"\r\n");
 				my_frame.ta_console.append(my_frame.txtbox_to.getText()+"\r\n");
 			}
 		
 		
 	}
 }
