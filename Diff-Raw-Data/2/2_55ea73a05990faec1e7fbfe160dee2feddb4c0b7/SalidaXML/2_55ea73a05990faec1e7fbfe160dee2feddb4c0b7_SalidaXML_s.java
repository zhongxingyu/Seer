 package principal;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class SalidaXML extends Salida{
 	
 	private File xmlFile;
 	private BufferedWriter writer;
 	
 	public SalidaXML(String fichero) throws IOException{
 		xmlFile = new File(fichero);
 		writer = new BufferedWriter(new FileWriter(xmlFile));		
 	}
 
 	@Override
 	public void setVenta(Venta venta) {
 		String xmlCode = "<ticket>" + '\n';
 		if(venta != null){			
 			for(LinVenta v : venta.getLinventas()){
 				String tab = "\t" + "\t";
 				xmlCode += "\t" + "<linTicket>" + "\n";
 				xmlCode += tab + "<descr>" + v.getProducto().getDescripcion() + "</descr>" + "\n";
 				xmlCode += tab + "<cant>" + v.getCantidad() + "</cant>" + "\n";
 				xmlCode += tab + "<pUnit>" + v.getProducto().getPvp() + "</pUnit>" + "\n";
 				//xmlCode += tab + "<dctoLin>" + v.getDescuentoLin() + "</dctoLin>" + '\n';
 				xmlCode += "\t" + "</linTicket>" + "\n";				
 			}
			xmlCode += "<totalAPagar> cant=\"" + venta.subtotal() + "\"</totalAPagar>" + "\n";
 			//xmlCode += "<dctoAcumulado> cant=\"" + venta.getDescuentoAcumulado() + "\"</dctoAcumulado>" + '\n';
 			//xmlCode += "<impuestos> cant=\"" + venta.getImpuestos() + "\"</impuestos>" + '\n';
 		}
 		
 		xmlCode += "</ticket>";
 		try {
 			writer.write(xmlCode);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		try {
 			writer.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 }
