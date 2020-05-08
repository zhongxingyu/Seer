 package cidc.certificaciones.pdf;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.ResourceBundle;
 
 import javax.servlet.http.HttpServletResponse;
 
 import cidc.certificaciones.obj.CertificacionesOBJ;
 import cidc.general.obj.Globales;
 import cidc.proyectosGeneral.obj.CoInvest;
 import cidc.proyectosGeneral.obj.Proyecto;
 
 import com.itextpdf.text.BaseColor;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.Image;
 import com.itextpdf.text.PageSize;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.Phrase;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.Barcode128;
 import com.itextpdf.text.pdf.BaseFont;
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfReader;
 import com.itextpdf.text.pdf.PdfStamper;
 import com.itextpdf.text.pdf.PdfTemplate;
 import com.itextpdf.text.pdf.PdfWriter;
 import com.itextpdf.text.pdf.RandomAccessFileOrArray;
 import com.itextpdf.text.pdf.codec.TiffImage;
 import com.lowagie.text.Watermark;
 
 public class GenerarCertificados {
 
 	public GenerarCertificados() {
 	}
 	
 	Globales global=new Globales();
 	char sep=java.io.File.separatorChar;
 	public PdfWriter writer=null;
 	public Document document = new Document(new PageSize().LETTER,80f,60f,140f,80f);
 	public ResourceBundle rbDir =ResourceBundle.getBundle("cidc.general.conect");
 	private static Font texto5 = new Font(Font.FontFamily.HELVETICA, 5,Font.NORMAL);
 	private static Font texto7 = new Font(Font.FontFamily.HELVETICA, 7,Font.NORMAL);
 	private static Font paginacion = new Font(Font.FontFamily.HELVETICA, 7,Font.NORMAL, BaseColor.DARK_GRAY);
 	private static Font texto9 = new Font(Font.FontFamily.HELVETICA, 9,Font.NORMAL);
 	private static Font texto9n = new Font(Font.FontFamily.HELVETICA, 9,Font.BOLD);
 	private static Font texto8 = new Font(Font.FontFamily.HELVETICA, 8,Font.NORMAL);
 	private static Font texto10 = new Font(Font.FontFamily.HELVETICA, 10,Font.NORMAL);
 	private static Font texto10n = new Font(Font.FontFamily.HELVETICA, 10,Font.BOLD);
 	private static Font texto10s = new Font(Font.FontFamily.HELVETICA, 10,Font.UNDERLINE);
 	private static Font texto11 = new Font(Font.FontFamily.HELVETICA, 11,Font.NORMAL);
 	private static Font texto11n = new Font(Font.FontFamily.HELVETICA, 11,Font.BOLD);
 	private static Font texto12n = new Font(Font.FontFamily.HELVETICA, 12,Font.BOLD);
 	private static Font texto15Blanca = new Font(Font.FontFamily.HELVETICA, 10,Font.BOLD, BaseColor.WHITE);
 	String numCertificado="";
 	String nombrePersona="";
 	
 	/**
 	 * Este metodo crea el pdf del certificado de paz y salvo
 	 * @param certificado
 	 * @param path
 	 * @param resp
 	 */
 	public void crearPazySalvo(CertificacionesOBJ certificado, String path, HttpServletResponse resp){
 		System.out.println("Creando Certificado Paz y Salvo");
 		String contenido=""; 
 		numCertificado=certificado.getConsCert()+" de "+Calendar.getInstance().get(Calendar.YEAR);
 		nombrePersona=certificado.getNombre().toUpperCase();		
 		Phrase []textoDocumento=new Phrase [2];
 		Phrase clausulas=new Phrase();
 		Phrase clausulasinicio=new Phrase();		
 		Date date = new Date();
 		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
 		String formattedDate = sdf.format(date);		
 		clausulasinicio.add(new Phrase("\n\nNmero de Verificacin: "+certificado.getCod_verificacion()+"\n",texto10n));
 		clausulasinicio.add(new Phrase("Generado el: "+formattedDate+"\n\n\n",texto10n));
 		clausulasinicio.add(new Phrase("El(La) investigador(a) ",texto10));
 		clausulasinicio.add(new Phrase("Persona de Prueba",texto10n));
 		clausulasinicio.add(new Phrase(" identificado(a) con la cédula de ciudadanía No.101010101 de Bogota, ",texto10));
 		clausulasinicio.add(new Phrase("a la fecha, se encuentra a ",texto10));
 		clausulasinicio.add(new Phrase("PAZ Y SALVO",texto10n));
 		clausulasinicio.add(new Phrase(" por concepto de proyectos de investigacin en el Centro de Investigacin y Desarrollo Cientfico. ",texto10));
 		clausulasinicio.add(new Phrase("Se expide la presente a solicitud del (de la) interesado(a) a los ",texto10));
 		clausulasinicio.add(new Phrase(" "+global.getDiaHoy()+" das del mes de "+global.getNombreMesHoy()+" de "+global.getAnoHoy()+".",texto10));
 		contenido=clausulasinicio.toString();
 		
 		textoDocumento[0]=clausulasinicio;
 		textoDocumento[1]=clausulas;
 //		PdfPTable tablaFirmas =new PdfPTable(2);
 		PdfPTable tablaEscudo =new PdfPTable(1);
 		PdfPCell c0 = null;
         Image firmaD=null;
         
         try {
         	/*tablaEscudo.setWidths(new float[]{(rectangulo.getLeft()+rectangulo.getRight()-120)});
         	tablaEscudo.setTotalWidth((rectangulo.getLeft()+rectangulo.getRight()-120));*/
 			tablaEscudo.setWidths(new int[]{200});
 			tablaEscudo.setTotalWidth(450);
 			tablaEscudo.getDefaultCell().setFixedHeight(70);
 			
 			firmaD=Image.getInstance(path.substring(0,path.lastIndexOf("Documentos"))+sep+"comp"+sep+"img"+sep+"firma_Director.png");
 			firmaD.setBorder(0);
 			tablaEscudo.getDefaultCell().setBorder(Rectangle.NO_BORDER);
 			tablaEscudo.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);			
 			
 			//      tablaFirmas.setWidths(new int[]{200,200});
 			//      tablaFirmas.setTotalWidth(450);
 			//      tablaFirmas.setLockedWidth(true);
 			//      tablaFirmas.getDefaultCell().setFixedHeight(10);
 			//      tablaFirmas.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
 			tablaEscudo.writeSelectedRows(0, 5, 72, 780 , writer.getDirectContent());
 		} catch (DocumentException e1) {
 			
 			e1.printStackTrace();
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		
         PdfPTable tablaBlanca =new PdfPTable(1);
         PdfPCell celdaVacia=new PdfPCell(new Phrase("\n....\n",texto15Blanca));
         
         celdaVacia.setBorder(Rectangle.NO_BORDER);
         tablaBlanca.addCell(celdaVacia);
         PdfPCell celdaGenerica=new PdfPCell();
         PdfPCell celdaTablaVacia=new PdfPCell(tablaBlanca);
         celdaTablaVacia.setBorder(Rectangle.NO_BORDER);
         celdaGenerica.setBorder(Rectangle.NO_BORDER);
 		
         PdfPCell ralla1=new PdfPCell(new Phrase("_________________________________ ",texto10n));
         PdfPCell directorCIDC=new PdfPCell(new Phrase(rbDir.getString("directorCIDC").toUpperCase(),texto10n));
 		PdfPCell tituloCIDC=new PdfPCell(new Phrase("Director Centro de Investigaciones",texto10));
 		
 		ralla1.setHorizontalAlignment(Element.ALIGN_CENTER);
 		ralla1.setBorder(Rectangle.NO_BORDER);		
 		
 		directorCIDC.setHorizontalAlignment(Element.ALIGN_CENTER);
 		directorCIDC.setBorder(Rectangle.NO_BORDER);		
 		
 		tituloCIDC.setHorizontalAlignment(Element.ALIGN_CENTER);
 		tituloCIDC.setBorder(Rectangle.NO_BORDER);		
 		
 		tablaEscudo.addCell(firmaD);		
 		tablaEscudo.addCell(ralla1);		
 		tablaEscudo.addCell(directorCIDC);		
 		tablaEscudo.addCell(tituloCIDC);		
 		try {
 			certificado.setCuerpo_cer(contenido);
 			inicarDocumentoCertificado(resp,path,certificado.getCod_verificacion());
 			agregarContenido(textoDocumento);
 			agregarPieDePagina();
 			document.close();
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		}
 		System.out.println("-Certificado Electronico creado->");
 
 		
 	}
 	/**
 	 * 
 	 * @param resp
 	 * @return
 	 */
 	private HttpServletResponse inicarDocumentoCertificado(HttpServletResponse resp, String pathArchivo,String codigo) {
 		try {
 			resp.setContentType("application/pdf");
 			resp.setHeader("Content-disposition","inline; filename=test.pdf"); 
 			writer=PdfWriter.getInstance(document, new FileOutputStream(pathArchivo));
 			PdfWriter.getInstance(document, new FileOutputStream("Certificado.pdf"));
 			
 			document.open();			
 			document.addTitle("\n EL SUSCRITO DIRECTOR DEL CENTRO DE INVESTIGACIONES Y DESARROLLO CIENTIFICO \n CERTIFICA QUE:");
 			document.addSubject("Certificado Electrnico");
 			document.addKeywords("Certificado, electrnico,o.j.a.s");
 			document.addAuthor("CIDC");
 			document.addCreator("Sistema SICIUD");
 			agregarCabecera(pathArchivo,codigo);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (DocumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 		return resp;
 	}
 	
 	/**
 	 * 
 	 * @param texto
 	 * @param tablaFirmas
 	 * @throws DocumentException
 	 */
 	private void agregarContenido(Phrase[] texto) throws DocumentException {
 		Paragraph total=new Paragraph();
 		for(int i=0;i<texto.length;i++)
 			total.add(texto[i]);
 		total.setAlignment(3);
 						
 		Paragraph p=new Paragraph("");
 		total.add(p);total.add(p);total.add(p);total.add(p);total.add(p);total.add(p);
 		document.add(total);		
 		//document.add(tablaFirmas);
 		//tablaFirmas.writeSelectedRows(100, 105, 72,30 , writer.getDirectContent());
 		
 	}
 	/**
 	 * 
 	 * @throws DocumentException
 	 */
 	private void agregarCabecera(String path, String codigo)	throws DocumentException {
 		System.out.println("agregando cabecera");
 		PdfPCell c0 = null;
 		Rectangle rectangulo=this.writer.getPageSize();
 		
 		PdfPTable tablaEscudo =new PdfPTable(1);
 		tablaEscudo.setWidths(new float[]{(rectangulo.getLeft()+rectangulo.getRight()-120)});
 		tablaEscudo.setTotalWidth((rectangulo.getLeft()+rectangulo.getRight()-120));
 		tablaEscudo.getDefaultCell().setFixedHeight(70);
 					
 		try {				
 			
 			Image escudo=Image.getInstance(path.substring(0,path.lastIndexOf("Documentos"))+sep+"comp"+sep+"img"+sep+"cabCertificado.png");
 			escudo.setBorder(0);
 			tablaEscudo.getDefaultCell().setBorder(Rectangle.NO_BORDER);
 			tablaEscudo.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
 			tablaEscudo.addCell(escudo);
 			tablaEscudo.addCell(codigoBarras(codigo));
 			c0=new PdfPCell(new Paragraph("\nEL SUSCRITO DIRECTOR DEL",texto11n));
 			c0.setHorizontalAlignment(Element.ALIGN_CENTER);
 			c0.setBorder(Rectangle.NO_BORDER);
 			tablaEscudo.addCell(c0);
 			PdfPCell c1 = null;
 			c1=new PdfPCell(new Paragraph("CENTRO DE INVESTIGACIONES Y DESARROLLO CIENTFICO",texto11n));
 			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
 			c1.setBorder(Rectangle.NO_BORDER);
 			tablaEscudo.addCell(c1);
 			PdfPCell c2 = null;
 			c2=new PdfPCell(new Paragraph("\nCERTIFICA",texto12n));
 			c2.setHorizontalAlignment(Element.ALIGN_CENTER);
 			c2.setBorder(Rectangle.NO_BORDER);
 			tablaEscudo.addCell(c2);
 			tablaEscudo.writeSelectedRows(0, 5, 72, 780 , writer.getDirectContent());
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void agregarPieDePagina() {
 		
 		try {
 			PdfPTable tablaFirma= new PdfPTable(1);
 			tablaFirma.setWidths(new int[] {400});
 			tablaFirma.setTotalWidth(400);
 			Image firmaD=null;
 			firmaD=Image.getInstance("D:/TOMCAT 6.0/webapps/siciud/"+"comp"+sep+"img"+sep+"firma_Director.png");
 			firmaD.setBorder(0);
 			firmaD.setAbsolutePosition(0, 0);
 			firmaD.scalePercent(20);
 			PdfPCell celdaImg = new PdfPCell(firmaD);
 			celdaImg.setHorizontalAlignment(Element.ALIGN_CENTER);
 			celdaImg.setBorder(Rectangle.NO_BORDER);
 			tablaFirma.addCell(celdaImg);
 			PdfPCell ralla1=new PdfPCell(new Phrase("_______________________________",texto10n));
 			ralla1.setHorizontalAlignment(Element.ALIGN_CENTER);
 			ralla1.setBorder(Rectangle.NO_BORDER);
 			tablaFirma.addCell(ralla1);
 			PdfPCell directorCIDC=new PdfPCell(new Phrase(rbDir.getString("directorCIDC").toUpperCase(),texto10n));
 			directorCIDC.setHorizontalAlignment(Element.ALIGN_CENTER);
 			directorCIDC.setBorder(Rectangle.NO_BORDER);
 			tablaFirma.addCell(directorCIDC);
 			PdfPCell tituloCIDC=new PdfPCell(new Phrase("Director Centro de Investigaciones",texto10));
 			tituloCIDC.setHorizontalAlignment(Element.ALIGN_CENTER);
 			tituloCIDC.setBorder(Rectangle.NO_BORDER);
 			tablaFirma.addCell(tituloCIDC);
 			PdfPCell vacia5=new PdfPCell(new Phrase("\n",texto10));
 			vacia5.setBorder(Rectangle.NO_BORDER);
 			tablaFirma.addCell(vacia5);
 			PdfPCell vacia6=new PdfPCell(new Phrase("\n",texto10));
 			vacia6.setBorder(Rectangle.NO_BORDER);
 			tablaFirma.addCell(vacia6);
 			PdfPCell vacia1=new PdfPCell(new Phrase("\n\n\n\n\n\n",texto10));
 			vacia1.setBorder(Rectangle.NO_BORDER);
 			tablaFirma.addCell(vacia1);
 			PdfPCell fecha = new PdfPCell((new Phrase("Este certificado fue firmado digitalmente el "+global.getDiaHoy()+" das del mes de "
 					+global.getNombreMesHoy()+" de "+global.getAnoHoy()+".",texto10s)));
 			fecha.setHorizontalAlignment(Element.ALIGN_CENTER);
 			fecha.setBorder(Rectangle.NO_BORDER);
 			tablaFirma.addCell(fecha);
 			tablaFirma.addCell(vacia1);
 			PdfPCell frase1=new PdfPCell(new Phrase("La autenticidad del contenido y la validez de la firma digital utilizada",texto8));
 			frase1.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			frase1.setBorder(Rectangle.NO_BORDER);
 			tablaFirma.addCell(frase1);
 			PdfPCell frase2=new PdfPCell(new Phrase("pueden ser verificadas en el portal web del Centro de Investigaciones y Desarrollo Cientfico",texto8));
 			frase2.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			frase2.setBorder(Rectangle.NO_BORDER);
 			tablaFirma.addCell(frase2);
 			PdfPCell frase3=new PdfPCell(new Phrase("http://cidc.udistrital.edu.co/web/",texto8));
 			frase3.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			frase3.setBorder(Rectangle.NO_BORDER);
 			tablaFirma.addCell(frase3);
 			PdfPCell frase4=new PdfPCell(new Phrase("Ingresando el nmero de certificado en la opcin: Servicios / Certificados en lnea / Verificar certificado",texto8));
 			frase4.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			frase4.setBorder(Rectangle.NO_BORDER);
 			tablaFirma.addCell(frase4);
 			document.add(tablaFirma);
 			
 			PdfPTable nuevoPiePdfPTable = new PdfPTable(2);
 			nuevoPiePdfPTable.setWidths(new int[] { 600,870 });
 			nuevoPiePdfPTable.setTotalWidth(560);
 			PdfPCell celda1= new PdfPCell(new Phrase("Centro de Investigaciones y Desarrollo Cientfico",paginacion));
 			celda1.setHorizontalAlignment(Element.ALIGN_LEFT);
 			celda1.setBorder(Rectangle.NO_BORDER);
 			nuevoPiePdfPTable.addCell(celda1);
 			
 			PdfPCell celda2= new PdfPCell(new Phrase("Universidad Distrital Francisco Jos de Caldas",paginacion));
 			celda2.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			celda2.setBorder(Rectangle.NO_BORDER);
 			nuevoPiePdfPTable.addCell(celda2);
 			
 			PdfPCell celda3= new PdfPCell(new Phrase("Web: cidc.udistrital.edu.co - Mail: cidc@udistrital.edu.co",paginacion));
 			celda3.setHorizontalAlignment(Element.ALIGN_LEFT);
 			celda3.setBorder(Rectangle.NO_BORDER);
 			nuevoPiePdfPTable.addCell(celda3);
 			
 			PdfPCell celda4= new PdfPCell(new Phrase("Carrera 7 No. 40B - 53 Piso 3",paginacion));
 			celda4.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			celda4.setBorder(Rectangle.NO_BORDER);
 			nuevoPiePdfPTable.addCell(celda4);
 			
 			PdfPCell celda5= new PdfPCell(new Phrase("Tel: 3 23 93 00 ext. 1329-1320",paginacion));
 			celda5.setHorizontalAlignment(Element.ALIGN_LEFT);
 			celda5.setBorder(Rectangle.NO_BORDER);
 			nuevoPiePdfPTable.addCell(celda5);
 			
 			PdfPCell celda6= new PdfPCell(new Phrase("Bogot D.C - Repblica de Colombia",paginacion));
 			celda6.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			celda6.setBorder(Rectangle.NO_BORDER);
 			nuevoPiePdfPTable.addCell(celda6);
 			
 			nuevoPiePdfPTable.writeSelectedRows(0, 8,30,45, writer.getDirectContent());
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * 
 	 * @param certificado clase objeto con la informacion para crear el certificado
 	 * @param path ruta donde se almacenara el certificado
 	 * @param resp
 	 * @return
 	 */
 	public HttpServletResponse crearCertificadoPertenencia(CertificacionesOBJ certificado, String path,  HttpServletResponse resp){
 		System.out.println("Creando Certificado Pertenencia a Grupo/Semillero de Investigacin");
 		String contenido=""; 
 		numCertificado=certificado.getConsCert()+" de "+global.getAnoHoy();
 		nombrePersona=certificado.getNombre().toUpperCase();		
 		Phrase []textoDocumento=new Phrase [20];
 		Phrase clausulas=new Phrase();
 		Phrase clausulasinicio=new Phrase();		
 //		Date date = new Date();
 //		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
 //		String formattedDate = sdf.format(date);		
 //		clausulasinicio.add(new Phrase("Generado el: "+formattedDate+"\n\n\n",texto10n));
 		clausulasinicio.add(new Phrase("\n\n\nEl(La) investigador(a) ",texto10));
 		clausulasinicio.add(new Phrase(certificado.getNombre().toUpperCase()+",",texto10n));
 		clausulasinicio.add(new Phrase(" identificado(a) con la cdula de ciudadana N "+certificado.getCedula()+" de "+ certificado.getNumCedDe()+" ",texto10));
 		clausulasinicio.add(new Phrase((certificado.getFechaSalida()!=null?"pertenecio":"pertenece"),texto10));
 		clausulasinicio.add(new Phrase(" desde el "+global.getDiaFecha(certificado.getFechaIngreso(), 1) +" de "+global.getNombreMes(certificado.getFechaIngreso(), 1) +" de "+ global.getAnoFecha(certificado.getFechaIngreso())+" ",texto10));
		if(certificado.getFechaSalida()!=null&&!certificado.getFechaSalida().equals("")){
 			clausulasinicio.add(new Phrase(" hasta el "+global.getDiaFecha(certificado.getFechaSalida(), 1) +" de "+global.getNombreMes(certificado.getFechaSalida(), 1) +" de "+ global.getAnoFecha(certificado.getFechaSalida())+" ",texto10));
 		}			
 		clausulasinicio.add(new Phrase("en calidad de "+certificado.getPapel()+" al "+certificado.getTipoGrupo()+" de investigacin ",texto10));
 		clausulasinicio.add(new Phrase("\""+certificado.getNombreGrupo()+"\"",texto10n));
 		clausulasinicio.add(new Phrase(", institucionalizado el ",texto10));
 		clausulasinicio.add(new Phrase(global.getDiaFecha(certificado.getFecha_cert(), 1) +" de "+global.getNombreMes(certificado.getFecha_cert(), 1) +" de "+ global.getAnoFecha(certificado.getFecha_cert()),texto10));
 		clausulasinicio.add(new Phrase(", por el Centro de Investigaciones y Desarrollo Cientfico de la Universidad Distrital Francisco Jos de Caldas ",texto10));
 		String categoria ="";
 		if(certificado.getCategoriaGrupo().equals("1")||certificado.getCategoriaGrupo().equals("2")){
 			categoria=(certificado.getCategoriaGrupo().equals("1"))?", el cual se encuentra sin clasificacin ":"";
 		}else{
 			categoria=", el cual se encuentra clasificado en categoria "+certificado.getCategoriaGrupo()+" por Colciencias en la convocatoria de grupos colombianos de investigacin.";
 		}
 		clausulasinicio.add(new Phrase(categoria,texto10));
 		clausulasinicio.add(new Phrase(" Bajo la direccin de la(el) profesor(a) "+certificado.getNombreDirector().toUpperCase()+".\n\n",texto10));
 		clausulasinicio.add(new Phrase("Se genera la presente certificacin a solucitud del (de la) interesado(a) a los ",texto10));
 		clausulasinicio.add(new Phrase(" "+global.getDiaHoy()+" das del mes de "+global.getNombreMesHoy()+" de "+global.getAnoHoy()+".",texto10));
 		contenido=clausulasinicio.toString()+" "+clausulas.toString();	
 		
 		textoDocumento[0]=clausulasinicio;
 		textoDocumento[1]=clausulas;
 		
 //		PdfPTable tablaFirmas =new PdfPTable(2);
 //		PdfPTable tablaEscudo =new PdfPTable(1);
 //		//Rectangle rectangulo=this.writer.getPageSize();
 //		PdfPCell c0 = null;
 //        Image firmaD=null;
 		
 		
 //		try {			
 //			/*tablaEscudo.setWidths(new float[]{(rectangulo.getLeft()+rectangulo.getRight()-120)});
 //			tablaEscudo.setTotalWidth((rectangulo.getLeft()+rectangulo.getRight()-120));*/
 ////			tablaEscudo.setWidths(new int[]{200});
 ////			tablaEscudo.setTotalWidth(450);
 ////			tablaEscudo.getDefaultCell().setFixedHeight(70);
 ////			
 ////			firmaD=Image.getInstance(path.substring(0,path.lastIndexOf("Documentos"))+sep+"comp"+sep+"img"+sep+"firma_Director.png");
 ////			firmaD.setBorder(0);
 ////			firmaD.setAbsolutePosition(0, 0);
 ////			tablaEscudo.getDefaultCell().setBorder(Rectangle.NO_BORDER);
 ////			tablaEscudo.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);			
 ////			
 ////			tablaFirmas.setWidths(new int[]{200,200});
 ////			tablaFirmas.setTotalWidth(450);
 ////			tablaFirmas.setLockedWidth(true);
 ////			tablaFirmas.getDefaultCell().setFixedHeight(10);
 ////			tablaFirmas.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
 //			//tablaEscudo.writeSelectedRows(0, 5, 0, 780 , writer.getDirectContent());
 //		} catch (DocumentException e1) {
 //			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 //		}catch (Exception e) {
 //			// TODO: handle exception
 //		}
 		
 //        PdfPTable tablaBlanca =new PdfPTable(1);
 //        PdfPCell celdaVacia=new PdfPCell(new Phrase("\n....\n",texto15Blanca));
 //        
 //        celdaVacia.setBorder(Rectangle.NO_BORDER);
 //        tablaBlanca.addCell(celdaVacia);
 //        PdfPCell celdaGenerica=new PdfPCell();
 //        PdfPCell celdaTablaVacia=new PdfPCell(tablaBlanca);
 //        celdaTablaVacia.setBorder(Rectangle.NO_BORDER);
 //        celdaGenerica.setBorder(Rectangle.NO_BORDER);
 		
 //        PdfPCell vacio=new PdfPCell(new Phrase("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" +
 //        		"\n\n\n\n\n\n\n\n\n\n\n\n\n",texto10n));
 //        vacio.setHorizontalAlignment(Element.ALIGN_LEFT);
 //        vacio.setBorder(Rectangle.NO_BORDER);
 //        
 //        PdfPCell ralla1=new PdfPCell(new Phrase("_______________________________",texto10n));
 //		PdfPCell directorCIDC=new PdfPCell(new Phrase(rbDir.getString("directorCIDC").toUpperCase(),texto10n));
 //		PdfPCell tituloCIDC=new PdfPCell(new Phrase("Director Centro de Investigaciones",texto10));
 //		
 //		ralla1.setHorizontalAlignment(Element.ALIGN_LEFT);
 //		ralla1.setBorder(Rectangle.NO_BORDER);		
 //		
 //		directorCIDC.setHorizontalAlignment(Element.ALIGN_LEFT);
 //		directorCIDC.setBorder(Rectangle.NO_BORDER);		
 //		
 //		tituloCIDC.setHorizontalAlignment(Element.ALIGN_LEFT);
 //		tituloCIDC.setBorder(Rectangle.NO_BORDER);	
 //		
 //		tablaEscudo.addCell(vacio);
 //		tablaEscudo.addCell(firmaD);		
 //		tablaEscudo.addCell(ralla1);		
 //		tablaEscudo.addCell(directorCIDC);		
 //		tablaEscudo.addCell(tituloCIDC);
 		
 	
 		
 		//*********************************+
 		try {
 			certificado.setCuerpo_cer(contenido);
 			inicarDocumentoCertificado(resp,path,certificado.getCod_verificacion());
 			agregarContenido(textoDocumento);
 			agregarPieDePagina();
 			document.close();
 			
 		} catch (DocumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("-Certificado Electrnico pertenencia creado->");
 		return resp;
 		
 	}
 	
 	public String marcaAgua(String path){
 		try {
 			String[] nombre= path.split("pdf");
 			String cadena =nombre[0].substring(0, nombre[0].length()-1);
 		  PdfReader pdfReader = new PdfReader(path);
 		  cadena+="_";
 	      PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(cadena+".pdf"));
 	      Image image = Image.getInstance("D:/TOMCAT 6.0/webapps/siciud/comp/img/water.jpg");
 	      for(int i=1; i<= pdfReader.getNumberOfPages(); i++){
 	          PdfContentByte content = pdfStamper.getUnderContent(i);
 	          image.setAbsolutePosition(10f, 8f);
 	          image.scalePercent(150);
 	          content.addImage(image);
 	      }
 	      pdfStamper.close();
 	      pdfReader.close();
 	      File file = new File(path);
 		  file.delete();
 	      System.out.println("finaliza con exiito");
 	      return cadena+".pdf";
 	    } catch (IOException e) {
 	      e.printStackTrace();
 	    } catch (DocumentException e) {
 	      e.printStackTrace();
 	    }
 		return null;
 	}
 	
 	public PdfPCell codigoBarras(String codigo){
 		PdfContentByte cimg = writer.getDirectContent();
 	  Barcode128 code128 = new Barcode128();
 	  code128.setCode(codigo);
 	  code128.setCodeType(Barcode128.CODE128);
 	  code128.setTextAlignment(Element.ALIGN_LEFT);
 	  Image image = code128.createImageWithBarcode(cimg, null, null);
 	  //float scaler = ((document.getPageSize().getWidth() - document.leftMargin()  - document.rightMargin() - 0) / image.getWidth()) * 140;
 	  image.scalePercent(80);
 	  image.setAlignment(Element.ALIGN_LEFT);
 	  PdfPCell cell = new PdfPCell(image);
 	  cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 	  cell.setBorder(Rectangle.NO_BORDER);
 	  return cell;
 	}
 	
 }
