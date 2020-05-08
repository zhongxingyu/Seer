 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Managers;
 
 import Beans.Category;
 import Beans.Order;
 import Beans.Product;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  *
  * @author Daniel
  */
 public class HtmlManager {
     
     public void printLoginPage(PrintWriter out , String message , int type)
           {
            out.println("<!DOCTYPE HTML>");   
            out.println("<html>");
            out.println("<head>");
            out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
            out.println("<link  href=\"Bootstrap/css/bootstrap.css\" rel=\"stylesheet\">");
            out.println("<link  href=\"Bootstrap/css/grafica.css\" rel=\"stylesheet\">");
            out.println("<script src=\"Bootstrap/js/jquery-1.8.2.js\"></script>");
            out.println("<script src=\"Bootstrap/js/bootstrap.min.js\"></script>");
            out.println("<title>Login</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<div class=\"login well\">");
            out.println("    <div class=\"login-title\">");
           out.println("        <h4>Benvenuti in Trento Ortofrutticola</h4><br>");
            out.println("    </div>");         
            out.println("        <form action=\"LoginController?op=login\" method=\"post\" class=\"form-horizontal\">");          
            out.println("        <div class=\"control-group\">");
            out.println("            <label class=\"control-label\" for=\"username\">Username</label>");
            out.println("                <div class=\"controls\">");
            out.println("                <input class=\"input-large\" placeholder=\"Username\" type=\"text\" id=\"username\" name=\"username\">");
            out.println("                </div>");
            out.println("         </div>");  
            out.println("        <div class=\"control-group\">");
            out.println("            <label class=\"control-label\" for=\"password\">Password</label>");
            out.println("                <div class=\"controls\">");
            out.println("                <input class=\"input-large\" placeholder=\"Password\" type=\"password\" id=\"password\" name=\"password\">");
            out.println("                </div>");
            out.println("            </div>");   
            out.println("        <div class=\"control-group\">");
            out.println("            <div class=\"controls\">");
            out.println("                <button class=\"btn\" type=\"submit\">Login</button>");
            out.println("                <button class=\"btn\" type=\"reset\" >Reset</button>");
            out.println("            </div>");
            out.println("        </div>");
            
            if(message != null)
              {  
                 out.println("<div align=\"center\" class=\"control-group\">");
                 if(type == -1)
                 {
                 out.println("   <div class=\"alert alert-error fade in\">");
                 out.println("   <a class=\"close\" data-dismiss=\"alert\" href=\"#\">&times;</a>");
                 out.println("   <p algin=\"center\" class=\"text-error\"> " + message + "</p>  ");
                 out.println("   </div>");
                 }
                 else
                 {
                 out.println("   <div class=\"alert alert-success fade in\">");
                 out.println("   <a class=\"close\" data-dismiss=\"alert\" href=\"#\">&times;</a>");
                 out.println("   <p algin=\"center\" class=\"text-success\"> " + message + "</p>  ");
                 out.println("   </div>");
                 }     
                 out.println("</div>");     
              }  
 
            out.println("    </form>");
            out.println("    </div>");
            out.println("</body>");
            out.println("</html>");   
 
           }
     
     static public void printErrorPage(PrintWriter out, String message , String redirectURL , String contextPath) //String message
     {
     out.println("<!DOCTYPE HTML>");       
     out.println("<html>");
     out.println("<head>");
     out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");     
     out.println("<link href=\""+contextPath+"/Bootstrap/css/bootstrap.css\" rel=\"stylesheet\">");
     out.println("<title>ErrorPage</title>");
     out.println("    <script type=\"text/javascript\">");
     out.println("       var ss = 5;");
     out.println("           function countdown() {");
     out.println("               ss = ss-1;");
     out.println("               if (ss==0) {");
     out.println("               window.location=\""+ redirectURL +"\";");
     out.println("           }");
     out.println("               else {");
     out.println("                   document.getElementById(\"countdown\").innerHTML=ss;");
     out.println("                   window.setTimeout(\"countdown()\", 1000);");
     out.println("               }}");
     out.println("    </script>");
     out.println("</head>");
     out.println("");
     out.println("<body onload=\"countdown()\">");
     out.println("    <center>");
     out.println("        <h5 class=\"text-error\">Non sei autorizzato ad accedere a questa pagina. <br> Sarai reindirizzato a breve a questo indirizzo <a href=\""+redirectURL+"\">"+message+"</a>(<span id=\"countdown\" style=\"color:green;\">5</span>)</h5>");
     out.println("    </center>");
     out.println("</body>");
     out.println("</html>");
 
     }
     
     public void printBuyerHomePage(PrintWriter out, ArrayList category_list)
     {
     out.println("<!DOCTYPE html>");
     out.println("<html><head>");
     out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
     out.println("<link href=\"/PrimoProgetto/Bootstrap/css/bootstrap.css\" rel=\"stylesheet\">");
     out.println("   <title>HomePage</title> </head> <body>");
     
     this.printPageHeader(out, category_list);
     
     out.println("       <div class=\"span10\">");
     out.println("           <table class=\"table\"> <tbody>");
     Iterator iter = category_list.iterator();
             while(iter.hasNext())
             {
             Category tmp = (Category) iter.next();
             out.println("<tr><td class=\"span3\"><img src=\""+ tmp.getImageURL() +"\" width=\"200\" height=\"200\" alt=\""+ tmp.getName() +"\"></td>");
             out.println("<td><a href=\"BuyerController?op=products&category=" + tmp.getId() + "\">"
                     + "<h4>"+ tmp.getName() +"</h4></a>" + tmp.getDescription() + "</td></tr>");
             }     
     out.println("           </tbody> </table> </div> </div> </div>");
     out.println("   </body>");
     out.println("</html>");
     
     
     }
     
     public void printBuyerProdcutsPage(PrintWriter out, ArrayList category_list , ArrayList products_list , int category_id)
     {
     
     out.println("<!DOCTYPE html>");
     out.println("<html><head>");
     out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
     out.println("<link href=\"/PrimoProgetto/Bootstrap/css/bootstrap.css\" rel=\"stylesheet\">");
     out.println("<script src=\"/PrimoProgetto/Bootstrap/js/jquery-1.8.2.js\"></script>");
     out.println("<script src=\"/PrimoProgetto/Bootstrap/js/bootstrap.min.js\"></script>");
     out.println("   <title>Lista Prodotti</title> </head> <body>");
 
     String category_name = this.printProductPageHeader(out, category_list , category_id);
         
     out.println("       <div class=\"span10\">"); 
     out.println("           <h3>"+ category_name +"</h3>");
         if(products_list.isEmpty()) {
             out.println("<h3>Non ci sono prodotto per questa categoria</h3>");
         }  
     out.println("           <table class=\"table\"> <tbody>");
     Iterator iter = products_list.iterator();
             while(iter.hasNext())
             {
             Product tmp = (Product) iter.next();
             out.println("<tr><td class=\"span3\"><img src=\""+ tmp.getImage_url() +"\" width=\"100\" height=\"100\" alt=\""+tmp.getProduct_name()+"\"></td>");
             out.println("<td><a href=\"BuyerOrderController?op=request&product=" + tmp.getProduct_id() + "\">"
                     + "<h5>"+ tmp.getProduct_name() +"</h5>"
                     + "</a><strong>Prezzo : <span style=\"color:red\">" + tmp.getPrice() + "</span></strong>$");
             out.println("<br><strong>Disponibilità : </strong>" + tmp.getQuantity() +" " + tmp.getUm());
             out.println("<br><p> <small> "+tmp.getDescription() +" </small></p>");
             }     
     out.println("           </tbody> </table> </div> </div> </div>");
     out.println("   </body>");
     out.println("</html>"); 
     }
     
     
     public void printBuyerOrdersPage(PrintWriter out, ArrayList category_list , ArrayList orders_list , String message , int type)
     {
     out.println("<!DOCTYPE html>");
     out.println("<html><head>");
     out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
     out.println("<link href=\"/PrimoProgetto/Bootstrap/css/bootstrap.css\" rel=\"stylesheet\">");
     out.println("<script src=\"/PrimoProgetto/Bootstrap/js/jquery-1.8.2.js\"></script>");
     out.println("<script src=\"/PrimoProgetto/Bootstrap/js/bootstrap.min.js\"></script>");
     out.println("   <title>I miei ordini</title> </head> <body>");
 
     this.printPageHeader(out, category_list);
         
     out.println("       <div class=\"span10\">");
     out.println("<h3>I miei ordini</h3>");
         if(orders_list.isEmpty()) {
             out.println("<h3>Non ci sono ordini effettuati</h3>");
         }
     if(message != null)
              {  
                 out.println("<div style=\"text-align:center\">");
                 if(type == -1)
                 {
                 out.println("   <div class=\"alert alert-error fade in\">");
                 out.println("   <a class=\"close\" data-dismiss=\"alert\" href=\"#\">&times;</a>");
                 out.println("   <h4><p algin=\"center\" class=\"text-error\"> " + message + "</p></h4> ");
                 out.println("   </div>");
                 }
                 else
                 {
                 out.println("   <div class=\"alert alert-success fade in\">");
                 out.println("   <a class=\"close\" data-dismiss=\"alert\" href=\"#\">&times;</a>");
                 out.println("   <h4><p algin=\"center\" class=\"text-success\"> " + message + "</p></h4> ");
                 out.println("   </div>");
                 }     
                 out.println("</div>");     
              }   
         
     out.println("           <table class=\"table\"> <tbody>");
     Iterator iter = orders_list.iterator();
             while(iter.hasNext())
             {
             Order tmp = (Order) iter.next();
             out.println("<tr><td class=\"span3\"><img src=\""+ tmp.getImage_url() +"\" width=\"100\" height=\"100\" alt=\""+tmp.getProduct_name()+"\"></td>");
             out.println("<td class=\"span6\"><h4>"+ tmp.getProduct_name() +"</h4>"
                     + "<strong>Ordinato in data : </strong>" + tmp.getOrder_date() + "<br>"
                     + "<strong>Ordine : </strong>#"+tmp.getOrder_id()+"<br>"
                     + "<strong>Venditore : </strong>" + tmp.getSeller_name()+ "</td>");
             out.println("<td>"
                     + "<strong><br>Prezzo: </strong>" + tmp.getPrice() + "$ * ");
             out.println(tmp.getQuantity()+" "+tmp.getUm()+" <br>");
             out.println("--------------------------------------------<br>");
             out.println("<strong>Totale : <span style=\"color:red\">" + tmp.getTotal_price() + "</span></strong>$<br>");
             out.println("<strong>Fattura : </strong><a href=\""+tmp.getReceipt_url()+"\" >Fattura</a></td></tr>");
             }     
     out.println("           </tbody> </table> </div> </div> </div>");
     out.println("   </body>");
     out.println("</html>");     
       
     }
     
     public void printBuyerOrderRequestPage(PrintWriter out , ArrayList category_list , Product product)
     {
     out.println("<!DOCTYPE html>");
     out.println("<html><head>");
     out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
     out.println("<link href=\"/PrimoProgetto/Bootstrap/css/bootstrap.css\" rel=\"stylesheet\">");
     out.println("<script type=\"text/javascript\">");
     
     out.println("function calculate(){");
     out.println("   var num = parseInt(document.getElementById(\"number\").value);");
     out.println("   var tot = num * "+product.getPrice()+";");
     out.println("   document.getElementById(\"totale\").innerHTML = +tot ;");
     out.println("   document.getElementById(\"update\").innerHTML = \"\";}"); 
     
     out.println("function check() {");
     out.println("   var num = parseInt(document.getElementById(\"number\").value);");
     out.println("   var max = "+product.getQuantity()+";");
     out.println("if(num <=0 ) document.getElementById(\"number\").value = 1;");
     out.println("else if(num >max) document.getElementById(\"number\").value = max;");
     out.println("document.getElementById(\"update\").innerHTML = \"(aggiorna prezzo)\"; }");
     
     out.println("function validate(form){");
     out.println("   if(document.getElementById(\"update\").innerHTML != \"\")");
     out.println("   {"); 
     out.println("           document.getElementById(\"messaggio\").innerHTML = \"*Devi aggiornare il prezzo prima di avanzare\"; ");
     out.println("           return false;}");
     out.println("    else return true;");
     out.println("    }"); 
     out.println("    </script> ");
     
     out.println("   <title>Carello</title> </head> <body>");
     
     this.printPageHeader(out, category_list);
     out.println("       <div class=\"span10\">");
        out.println("<h3>Il mio carrello</h3>");
        out.println("           <table class=\"table\"> <tbody>");
        out.println("<tr><td class=\"span5\"><img src=\""+ product.getImage_url() +"\" width=\"300\" height=\"300\" alt=\""+product.getProduct_name()+"\"></td>");
        out.println("<td><p><h4>"+ product.getProduct_name() + "</h4>");   
        out.println("<small>"+product.getDescription()+"</small><br>");
        out.println("<strong>Venduto da : </strong> "+product.getSeller_name() + "<br>");
        out.println("<strong>Prezzo : </strong> "+product.getPrice() + "$<br>");
        out.println("<strong>Disponibili : </strong> "+product.getQuantity() + " " + product.getUm() + "<br>");
        out.println("--------------------------------------------<br></p>");
        out.println("<strong>Prezzo totale : <span style=\"color:red\" id=\"totale\">"+product.getPrice()+"</span></strong>$"); 
        out.println("                  &ensp;<span style=\"color:red\" id=\"messaggio\"></span><br></p>");
        out.println("        <form action=\"BuyerOrderController?op=confirm\" onsubmit=\"return validate(this)\" method=\"post\">");          
        out.println("        <div class=\"control-group\">");
        out.println("            <span><strong>Quantità</strong></span>&ensp;<a href=\"#\" onclick=\"calculate(); return false\" id=\"update\"></a>");
        out.println("                <div class=\"controls\">");
        out.println("                <input class=\"input-small\" type=\"number\" id=\"number\" name=\"number\" value=\"1\" onchange=\"check()\">");
        out.println("                </div>");
        out.println("        </div>");  
        out.println("        <div class=\"control-group\">");
        out.println("            <div class=\"controls\">");
        out.println("                <button class=\"btn\" type=\"submit\">Conferma ordina</button>");
        out.println("                <a type=\"button\" class=\"btn\" href=\"BuyerController?op=products&category="+product.getCategory_id()+"\">Annulla ordine</a>");
        out.println("            </div>");
        out.println("        </div> </td></tr>");
        out.println("           </tbody> </table> </div> </div> </div>");
        out.println("   </body>");
        out.println("</html>"); 
     }
     
     public void printBuyerOrderConfirmPage(PrintWriter out , ArrayList category_list ,Product product)
     {
     out.println("<!DOCTYPE html>");
     out.println("<html><head>");
     out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
     out.println("<link href=\"/PrimoProgetto/Bootstrap/css/bootstrap.css\" rel=\"stylesheet\">");   
     out.println("   <title>Conferma ordine</title> </head> <body>");
     
     this.printPageHeader(out, category_list);
        out.println("       <div class=\"span10\">");
        out.println("<h3>Conferma ordine</h3>");
        out.println("           <table class=\"table\"> <tbody>");
        out.println("<tr><td class=\"span5\"><img src=\""+ product.getImage_url() +"\" width=\"300\" height=\"300\" alt=\""+product.getProduct_name()+"\"></td>");
        out.println("<td><p><h4>"+ product.getProduct_name() + "</h4>");   
        out.println("<small>"+product.getDescription()+"</small><br>");
        out.println("<strong>Venduto da : </strong>"+product.getSeller_name() + "<br>");
        out.println("<strong>Prezzo : </strong>"+product.getPrice() + "$<br>");
        out.println("<strong>Pezzi acquistati : </strong>"+product.getQuantity() + " " + product.getUm() + "<br>");
        out.println("--------------------------------------------<br></p>");
        out.println("<strong>Prezzo totale ordine : <span style=\"color:red\" id=\"totale\">"+product.getPrice()*product.getQuantity()+"</span></strong>$"); 
        out.println("                  &ensp;<span style=\"color:red\" id=\"messaggio\"></span><br></p>");
        out.println("        <form action=\"BuyerOrderController?op=response\" method=\"post\">");     
        out.println("                <input type=\"hidden\" name=\"prec_op\" value=\"confirm\">");
        out.println("        <div class=\"control-group\">");
        out.println("            <div class=\"controls\">");
        out.println("                <button class=\"btn\" type=\"submit\">Conferma ordine</button>");
        out.println("                <a type=\"button\" class=\"btn\" href=\"BuyerOrderController?op=cancel\">Annulla ordine</a>"); 
        out.println("            </div>");
        out.println("        </div> </td></tr>");
        out.println("           </tbody> </table> </div> </div> </div>");
        out.println("   </body>");
        out.println("</html>"); 
     }
     
     
     
     
     //Questo stampa solo l'header
     private void printPageHeader(PrintWriter out , ArrayList category_list)
     {
     out.println("   <div class=\"row-fluid\">");
     out.println("       <div class=\"span12 \">");
     out.println("           <div class=\"row-fluid\">");
     out.println("               <div class=\"span10\"><img src=\"/PrimoProgetto/Images/logo.jpg\" alt=\"logo\"> </div>");
     out.println("   </div></div></div><br>");
     out.println("<div class=\"container-fluid\">");
     out.println("  	<div class=\"row-fluid\">");
     out.println("           <div class=\"span2\" style=\"min-width:140px\">");
     out.println("               <ul class=\"nav nav-list\">");
     out.println("                   <li  ><a href=\"BuyerController?op=home\">Home</a></li>");
     out.println("                   <li class=\"nav-header\">Il mio account</li>");
     out.println("                   <li><a href=\"BuyerController?op=orders\">I miei ordini</a></li>");
     out.println("                   <li><a href=\"/PrimoProgetto/LoginController?op=logout\">Logout</a></li>");
     out.println("                   <li class=\"nav-header\">Categorie</li>");
     Iterator iter = category_list.iterator();
     while(iter.hasNext())
             {
             Category tmp = (Category) iter.next();
             out.println("<li><a href=\"BuyerController?op=products&category="+tmp.getId()+"\">"+tmp.getName()+"</a></li>");
             }    
     out.println("		</ul></div>");
     }
     
     
     //Questo ritorna il nome della categoria del prodotto
     private String printProductPageHeader(PrintWriter out , ArrayList category_list, int category_id)
     {
     out.println("   <div class=\"row-fluid\">");
     out.println("       <div class=\"span12 \">");
     out.println("           <div class=\"row-fluid\">");
     out.println("               <div class=\"span10\"><img src=\"/PrimoProgetto/Images/logo.jpg\" alt=\"logo\"> </div>");
     out.println("   </div></div></div><br>");
     out.println("<div class=\"container-fluid\">");
     out.println("  	<div class=\"row-fluid\">");
     out.println("           <div class=\"span2\" style=\"min-width:140px\">");
     out.println("               <ul class=\"nav nav-list\">");
     out.println("                   <li  ><a href=\"BuyerController?op=home\">Home</a></li>");
     out.println("                   <li class=\"nav-header\">Il mio account</li>");
     out.println("                   <li><a href=\"BuyerController?op=orders\">I miei ordini</a></li>");
     out.println("                   <li><a href=\"/PrimoProgetto/LoginController?op=logout\">Logout</a></li>");
     out.println("                   <li class=\"nav-header\">Categorie</li>");
     Iterator iter = category_list.iterator();
     String result = null;
     while(iter.hasNext())
             {
             Category tmp = (Category) iter.next();
             if(category_id == tmp.getId()) {
                     result = tmp.getName();
                 }
             out.println("<li><a href=\"BuyerController?op=products&category="+tmp.getId()+"\">"+tmp.getName()+"</a></li>");
             }    
     out.println("		</ul></div>");
         return result;
     }
     
     
     
     
 }
