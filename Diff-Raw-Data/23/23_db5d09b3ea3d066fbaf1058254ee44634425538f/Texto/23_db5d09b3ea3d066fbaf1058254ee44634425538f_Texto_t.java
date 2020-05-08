 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package SistemaRecombinador;
 
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 
 /**
  *
  * @author Guilherme
  */
 public class Texto {
     
     private List<String> texto;
 
     public Texto() {
         this.texto = new LinkedList<String>();
     }
 
     
     
     public Texto(String texto) {
         this.texto = divideEmLinhas(texto);
         
     }
     
     private List divideEmLinhas(String texto){
         texto = texto.replaceAll("\n", " ");
         String[] textoDividido = texto.split(" ");
         List<String> textoFinal = new LinkedList<String>();
         
         int posicaoAtualNaBusca = 0;
         String linha = "";
         for (String string : textoDividido) {
             
             linha += string;
             
            if(posicaoAtualNaBusca == 9) {
                 posicaoAtualNaBusca = 0;
                 textoFinal.add(linha);
                 linha = "";
             }
             else{
                 posicaoAtualNaBusca++;
                 linha += " ";
             }
                 
         }
         if(!linha.equals("")){
             textoFinal.add(linha);
         }
         return textoFinal;
         
             
     }
         
     
 
     public Collection<String> getTexto() {
         return texto;
     }
 
    
     @Override
     public boolean equals(Object o) {
         if(o instanceof Texto){
             return this.texto.equals(((Texto)o).getTexto());
         }
         return false;
     }
     
    
     public String getFirst12Words(){
        int i = 0;
        String first12 = "";
        for (String string : texto) {
            
            for (String string1 : string.split(" ")) {
                if(i  == 12) {
                    break;
                }
                if(i != 0){
                    first12 += " ";
                }
                first12 += string1;
                i++;
            }
            if(i  == 12){
                break;
            }
        }
        return first12;
     }
     
     public ListIterator<String> listIterator(){
         return texto.listIterator();
     }
         
     
     public boolean contains(String linha){
         return this.texto.contains(linha);
     }
     
     public void addLinha(String linha){
         this.texto.add(linha);
     }
     
     public int numeroDeLinhas(){
         return this.texto.size();
     }
         
     
     
     
 }
