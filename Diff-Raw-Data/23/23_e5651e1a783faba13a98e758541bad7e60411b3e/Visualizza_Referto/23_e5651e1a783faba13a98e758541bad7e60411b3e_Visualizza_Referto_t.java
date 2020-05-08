 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ospedale;
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.Font.FontFamily;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.pdf.PdfWriter;
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 
 /**
  *
  * @author alex
  */
 public class Visualizza_Referto extends JFrame {
     private Database db=new Database();
     private String testo;
     private JTextArea foglio;
     private String id;
     private Paziente paz;
    JButton stampa,salva,chiudi;
     
     public Visualizza_Referto(String idPrenot,boolean editabile){
       id=idPrenot;
       
         Container container = getContentPane();
     foglio=new JTextArea();
     foglio.setEditable(editabile);
     foglio.setLineWrap(true);
     foglio.setWrapStyleWord(true);
    stampa=new JButton("STAMPA");
    salva=new JButton("SALVA");
    chiudi=new JButton("CHIUDI");
     JLabel titolo=new JLabel("REFERTO n° "+ id);
     
     this.setTitle("REFERTO n° "+ id);
     JPanel inputPanel1=new JPanel();
     inputPanel1.add(titolo);
     JPanel inputPanel2=new JPanel();
     inputPanel2.add(stampa);
     inputPanel2.add(salva);
     inputPanel2.add(chiudi);
     container.add(inputPanel1,BorderLayout.NORTH);
     container.add(new JScrollPane(foglio),BorderLayout.CENTER);
     container.add(inputPanel2,BorderLayout.SOUTH);
     testo=null;
     String SQL="select referto from referti where idprenotazione='"+id+"';";
     
     try{
             db.connetti();
             ResultSet r=db.eseguiQuery(SQL);
             while(r.next()){
                 testo=r.getString("referto");
                 System.out.println(testo);
             }
             
         } catch(SQLException e){e.getMessage();}
     if (testo==null && editabile==false){
         foglio.setText("Referto non ancora inserito");
         stampa.setEnabled(false);
         salva.setEnabled(false);
     }else if (testo!=null && editabile==false){
         foglio.setText(testo);
         stampa.setEnabled(true);
         salva.setEnabled(false);
     }
     
     if (testo==null && editabile==true){
         stampa.setEnabled(false);
     }else if (testo!= null && editabile==true){
         foglio.setText(testo);
     }
     
     setDefaultCloseOperation(EXIT_ON_CLOSE);
         setSize(450, 530);
         setVisible(true);
         setResizable(false);
         
         
         chiudi.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chiudiActionPerformed(evt);
             }
         });
         
         salva.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 salvaActionPerformed(evt);
             }
         });
         
         stampa.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 try {
                     try {
                         stampaActionPerformed(evt);
                     } catch (SQLException ex) {
                         Logger.getLogger(Visualizza_Referto.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } catch (DocumentException ex) {
                     Logger.getLogger(Visualizza_Referto.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (FileNotFoundException ex) {
                     Logger.getLogger(Visualizza_Referto.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
     
     }
     
     private void chiudiActionPerformed(java.awt.event.ActionEvent evt) {                                                     
         this.setVisible(false);
     }
     
     private void salvaActionPerformed(java.awt.event.ActionEvent evt) {                                                     
         System.out.println(foglio.getText());
         if(foglio.getText()!=null && foglio.getText().isEmpty()==false){
             String SQL="update referti set referto='"+foglio.getText()+"' where idprenotazione='"+id+"';";
             db.eseguiAggiornamento(SQL);
             System.out.println("aggiornato");
            if(foglio.getText()!=null && foglio.getText().isEmpty()==false){
                stampa.setEnabled(true);
            }
             
         }else {System.out.println("ricon vuoto, settato a null campo db");
             String SQL="update referti set referto=null where idprenotazione='"+id+"';";
             db.eseguiAggiornamento(SQL);
            stampa.setEnabled(false);
            
         }
     }
     
     private void stampaActionPerformed(java.awt.event.ActionEvent evt) throws DocumentException, FileNotFoundException, SQLException {                                                     
         db.connetti();
         
         String SQL="select reparto,data,ora,idpaziente from prenotazioni where idprenotazione='"+id+"';";
         ResultSet rs=db.eseguiQuery(SQL);
         String reparto=null;
         String data=null;
         String ora=null;
         String paziente=null;
         while(rs.next()){
             reparto=rs.getString("reparto");
             data=rs.getString("data");
             ora=rs.getString("ora");
             paziente=rs.getString("idpaziente");
         }
         
         
         SQL="select nome,email,telefono,residenza from utenti where cod_fisc='"+paziente+"';";
         rs=db.eseguiQuery(SQL);
         String nome=null;
         String email=null;
         String telefono=null;
         String residenza=null;
         while(rs.next()){
             nome=rs.getString("nome");
             email=rs.getString("email");
             telefono=rs.getString("telefono");
             residenza=rs.getString("residenza");
         }
         
         
         db.disconnetti();
         
         
         //definiamo il nome del nostro file di prova
         String filename= "Referto n°"+id+" - "+paziente+".pdf";
         // Creiamo un Document
         Document document = new Document();
         // otteniamo una istanza di PdfWriter passando il document ed uno stream file
         PdfWriter.getInstance(document, new FileOutputStream(filename));
         // apriamo il documento
         document.open();
         // aggiungiamo i paragrafi
         Font fontTitolo=new Font(FontFamily.HELVETICA, 24, Font.BOLD);
         Paragraph titolo=new Paragraph("REFERTO N° "+id+"\n\n",fontTitolo);
         document.add(titolo);
         document.add(new Paragraph("CODICE FISCALE: "+paziente+"\n"+
                                     "COGNOME E NOME: "+nome+"\n"+
                                     "INDIRIZZO MAIL: "+email+"\n"+
                                     "NUMERO TELEFONICO: "+telefono+"\n"+
                                     "INDIRIZZO: "+residenza+"\n\n"));
 
         Font fontVisita=new Font(FontFamily.HELVETICA, 12, Font.BOLD);
         Paragraph visita=new Paragraph("Visita effettuata nel reparto: "+reparto+"   in data: "+data+"   alle ore: "+ora+"\n\n",fontVisita);
         document.add(visita);
         document.add(new Paragraph(foglio.getText()));
         // chiudiamo il documento
         document.close();
         JOptionPane.showMessageDialog(null,"Il referto è stato stampato");
 
     }
 
    
 }
