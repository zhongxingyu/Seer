 /**
  *   Copyright 2013 Nekorp
  *
  *Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License
  */
 
 package org.nekorp.workflow.desktop.servicio.reporte.orden.servicio;
 
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageWriteParam;
 import javax.imageio.ImageWriter;
 import javax.imageio.stream.FileImageOutputStream;
 import org.apache.batik.dom.GenericDOMImplementation;
 import org.apache.batik.svggen.SVGGraphics2D;
 import org.apache.commons.lang.StringUtils;
 import org.nekorp.workflow.desktop.modelo.reporte.orden.servicio.DatosAutoOS;
 import org.nekorp.workflow.desktop.modelo.reporte.orden.servicio.DatosClienteOS;
 import org.nekorp.workflow.desktop.modelo.reporte.orden.servicio.DatosOS;
 import org.nekorp.workflow.desktop.modelo.reporte.orden.servicio.DetalleCostoOS;
 import org.nekorp.workflow.desktop.modelo.reporte.orden.servicio.InventarioDamageOS;
 import org.nekorp.workflow.desktop.modelo.reporte.orden.servicio.ParametrosReporteOS;
 import org.nekorp.workflow.desktop.view.model.bitacora.EventoEntregaVB;
 import org.nekorp.workflow.desktop.view.model.bitacora.EventoGeneralVB;
 import org.nekorp.workflow.desktop.view.model.bitacora.EventoObservacionesVB;
 import org.nekorp.workflow.desktop.view.model.bitacora.EventoVB;
 import org.nekorp.workflow.desktop.view.model.costo.CostoMetadata;
 import org.nekorp.workflow.desktop.view.model.costo.RegistroCostoVB;
 import org.nekorp.workflow.desktop.view.model.costo.RegistroHojalateriaPinturaVB;
 import org.nekorp.workflow.desktop.view.model.costo.RegistroMecanicaVB;
 import org.nekorp.workflow.desktop.view.model.inventario.damage.DamageDetailsVB;
 import org.nekorp.workflow.desktop.view.model.servicio.ServicioVB;
 import org.nekorp.workflow.desktop.view.resource.ShapeView;
 import org.nekorp.workflow.desktop.view.resource.imp.DamageDetailGraphicsView;
 import org.nekorp.workflow.desktop.view.resource.imp.IndicadorBarraGraphicsView;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 
 /**
  *
  */
 @Service
 public class OrdenServicioDataFactory {
     @Autowired
     @Qualifier(value = "servicio")
     private ServicioVB servicio;
     @Autowired
     private CostoMetadata costoMetadata;
     @Autowired
     @Qualifier("autoCuatroRightView")
     private ShapeView autoRightView;
     @Autowired
     @Qualifier("autoCuatroLeftView")
     private ShapeView autoLeftView;
     @Autowired
     @Qualifier("autoCuatroFrontView")
     private ShapeView autoFrontView;
     @Autowired
     @Qualifier("autoCuatroRearView")
     private ShapeView autoRearView;
     private String dateFormat = "dd-MMMM-yyyy";
     private String monedaFormat = "$#,##0.00";
     private int esquemaFontSize = 16;
     
     public List<DatosOS> getDatosOS(ParametrosReporteOS param) {
         try {
             DatosOS r = new DatosOS();
             File logo = new File("logo-ACE.svg");
             r.setLogo(logo.getCanonicalPath());
             r.setNoServicio(servicio.getId());
             r.setFecha(buscaFechaRecepcionAuto());
             r.setDatosCliente(getDatosCliente());
             r.setAsesor(buscaAsesor());
             r.setDatosAuto(getDatosAuto());
             r.setObservaciones(buscarObservaciones());
             r.setCosto(getDetalleCosto(param.isConCosto()));
             if (param.isConCosto()) {
                 DecimalFormat df = new DecimalFormat(monedaFormat);
                 r.setTotalCosto(df.format(costoMetadata.getTotalSinOtros().doubleValue()));
             } else {
                 r.setTotalCosto("");
             }
             r.setRecomendaciones(buscarRecomendaciones());
             r.setInventarioDamage(generaInventarioDamageOSSVG());
             List<DatosOS> lista = new LinkedList<>();
             lista.add(r);
             return lista;
         } catch (IOException ex) {
             throw new IllegalArgumentException(ex);
         }
     }
     
     private String buscaFechaRecepcionAuto() {
         for (EventoVB x: this.servicio.getBitacora().getEventos()) {
             if (x instanceof EventoGeneralVB) {
                 EventoGeneralVB y = (EventoGeneralVB) x;
                 if (StringUtils.equalsIgnoreCase(y.getEtiquetas(), "cotización") || StringUtils.equalsIgnoreCase(y.getEtiquetas(), "cotizacion")) {
                    SimpleDateFormat f = new SimpleDateFormat(dateFormat);
                     return StringUtils.lowerCase(f.format(y.getFechaEvento()));
                 }
             }
         }
         for (EventoVB x: this.servicio.getBitacora().getEventos()) {
             if (x instanceof EventoEntregaVB) {
                 EventoEntregaVB y = (EventoEntregaVB) x;
                 if (StringUtils.equals(y.getNombreEvento(), "Entrada de Auto")) {
                    SimpleDateFormat f = new SimpleDateFormat(dateFormat);
                     return StringUtils.lowerCase(f.format(y.getFecha()));
                 }
             }
         }
        return "";
     }
     
     private String buscaAsesor() {
         for (EventoVB x: this.servicio.getBitacora().getEventos()) {
             if (x instanceof EventoEntregaVB) {
                 EventoEntregaVB y = (EventoEntregaVB) x;
                 if (StringUtils.equals(y.getNombreEvento(), "Entrada de Auto")) {
                     return y.getResponsable();
                 }
             }
         }
         return "";
     }
     
     private String buscarObservaciones() {
         String r = "";
         for (EventoVB x: this.servicio.getBitacora().getEventos()) {
             if (x instanceof EventoObservacionesVB) {
                 EventoObservacionesVB y = (EventoObservacionesVB) x;
                 if (!StringUtils.isEmpty(r)) {
                     r = r + ", ";
                 }
                 String s = y.getObservaciones();
                 s = StringUtils.replace(s, "\n", " / ");
                 r = r + s;
             }
         }
         return r;
     }
     
     private String buscarRecomendaciones() {
         String r = "";
         for (EventoVB x: this.servicio.getBitacora().getEventos()) {
             if (x instanceof EventoGeneralVB) {
                 EventoGeneralVB y = (EventoGeneralVB) x;
                 if (StringUtils.equalsIgnoreCase(StringUtils.trim(y.getEtiquetas()), "recomendaciones") 
                         && !StringUtils.isEmpty(y.getDetalle())) {
                     if (!StringUtils.isEmpty(r)) {
                         r = r + "\n";
                     }
                     r = r + y.getDetalle();
                 }
             }
         }
         return r;
     }
     
     private DatosClienteOS getDatosCliente() {
         DatosClienteOS cliente = new DatosClienteOS();
         cliente.setNombre(servicio.getCliente().getNombre());
         String direccion = "";
         direccion = concatena(direccion, servicio.getCliente().getDomicilio().getCalle(), " ");
         direccion = concatena(direccion, servicio.getCliente().getDomicilio().getNumInterior(), " ");
         direccion = concatena(direccion, servicio.getCliente().getDomicilio().getColonia(), ", ");
         direccion = concatena(direccion, servicio.getCliente().getDomicilio().getCiudad(), ", ");
         if (!StringUtils.isEmpty(servicio.getCliente().getDomicilio().getCodigoPostal())) {
             direccion = concatena(direccion, "CP:" + servicio.getCliente().getDomicilio().getCodigoPostal(), ", ");
         }
         cliente.setDireccion(direccion);
         cliente.setEmail(servicio.getCliente().getEmail());
         String telefono = "";
         telefono = concatena(telefono, servicio.getCliente().getTelefonoUno().getValor(), "/");
         telefono = concatena(telefono, servicio.getCliente().getTelefonoDos().getValor(), "/");
         telefono = concatena(telefono, servicio.getCliente().getTelefonoTres().getValor(), "/");
         cliente.setTelefono(telefono);
         return cliente;
     }
         
     private String concatena(String cadena, String anexo, String separador) {
         String r = cadena;
         if (StringUtils.isEmpty(anexo)) {
             return cadena;
         }
         if (!StringUtils.isEmpty(cadena)) {
             r = r + separador;
         }
         return r + anexo;
     }
     
     private DatosAutoOS getDatosAuto() {
         DatosAutoOS auto = new DatosAutoOS();
         auto.setMarca(servicio.getAuto().getMarca());
         auto.setTipo(servicio.getAuto().getTipo());
         auto.setModelo(servicio.getAuto().getModelo());
         auto.setColor(servicio.getAuto().getColor());
         auto.setPlacas(servicio.getAuto().getPlacas());
         auto.setKilometraje(servicio.getDatosAuto().getKilometraje());
         auto.setSerie(servicio.getAuto().getNumeroSerie());
         String descServ = servicio.getDescripcion();
         descServ = StringUtils.replace(descServ, "\n", " / ");
         auto.setServicio(descServ);
         double porcentajeNivel;
         try {
             porcentajeNivel = Double.parseDouble(servicio.getDatosAuto().getCombustible()) / 100d;
         } catch (NumberFormatException e) {
             porcentajeNivel = 0;
         }
         auto.setNivelCombustible(generaImagenCombustibleSVG(porcentajeNivel));
         return auto;
     }
     
     private List<DetalleCostoOS> getDetalleCosto(boolean conCosto) {
         List<DetalleCostoOS> lista = new LinkedList<>();
         DetalleCostoOS det;
         List<RegistroCostoVB> datos = servicio.getCostos();
         Collections.sort(datos);
         for (RegistroCostoVB x: datos) {
             if (x instanceof RegistroHojalateriaPinturaVB || x instanceof RegistroMecanicaVB) {
                 det = new DetalleCostoOS();
                 det.setCantidad(x.getCantidad().toString());
                 det.setDescripcion(calculaDescripcion(x));
                 if (conCosto) {
                     DecimalFormat df = new DecimalFormat(monedaFormat);
                     det.setCosto(df.format(x.getSubtotal().doubleValue()));
                 } else {
                     det.setCosto("");
                 }
                 lista.add(det);
             }
         }
         return lista;
     }
     
     private String calculaDescripcion(RegistroCostoVB x) {
         String descripcion = "";
         if (x instanceof RegistroHojalateriaPinturaVB) {
             descripcion = concatena(descripcion,"HP", "");
         }
         if (x instanceof RegistroMecanicaVB) {
             descripcion = concatena(descripcion,"ME", "");
         }
         if (StringUtils.equals(x.getSubtipo(), "Mano de Obra")) {
             descripcion = concatena(descripcion,"M", "-");
         }
         if (StringUtils.equals(x.getSubtipo(), "Refacciones")) {
             descripcion = concatena(descripcion,"R", "-");
         }
         if (StringUtils.equals(x.getSubtipo(), "Insumo")) {
             descripcion = concatena(descripcion,"I", "-");
         }
         descripcion = concatena(descripcion, x.getConcepto(), ": ");
         return descripcion;
     }
     
     private InventarioDamageOS generaInventarioDamageOS() {
         try {
             InventarioDamageOS inv = new InventarioDamageOS();
             File derecha = new File("data/derecha.jpg");
             File izquierda = new File("data/izquierda.jpg");
             File frontal = new File("data/frontal.jpg");
             File trasera = new File("data/trasera.jpg");
             this.generaImagenDamage(autoRightView, servicio.getDatosAuto().getDamage().getDerecha(), derecha, 1200, 410);
             this.generaImagenDamage(autoLeftView, servicio.getDatosAuto().getDamage().getIzquierda(), izquierda, 1200, 410);
             this.generaImagenDamage(autoFrontView, servicio.getDatosAuto().getDamage().getFrontal(), frontal, 700, 410);
             this.generaImagenDamage(autoRearView, servicio.getDatosAuto().getDamage().getTrasera(), trasera, 700, 410);
             inv.setDerecha(derecha.getCanonicalPath());
             inv.setIzquierda(izquierda.getCanonicalPath());
             inv.setFrontal(frontal.getCanonicalPath());
             inv.setTrasera(trasera.getCanonicalPath());
             return inv;
         } catch (IOException ex) {
             throw new RuntimeException(ex); 
         }
     }
     
     private InventarioDamageOS generaInventarioDamageOSSVG() {
         try {
             InventarioDamageOS inv = new InventarioDamageOS();
             File derecha = new File("data/derecha.svg");
             File izquierda = new File("data/izquierda.svg");
             File frontal = new File("data/frontal.svg");
             File trasera = new File("data/trasera.svg");
             this.generaSVGImagenDamage(autoRightView, servicio.getDatosAuto().getDamage().getDerecha(), derecha, 1200, 410);
             this.generaSVGImagenDamage(autoLeftView, servicio.getDatosAuto().getDamage().getIzquierda(), izquierda, 1200, 410);
             this.generaSVGImagenDamage(autoFrontView, servicio.getDatosAuto().getDamage().getFrontal(), frontal, 800, 410);
             this.generaSVGImagenDamage(autoRearView, servicio.getDatosAuto().getDamage().getTrasera(), trasera, 800, 410);
             inv.setDerecha(derecha.getCanonicalPath());
             inv.setIzquierda(izquierda.getCanonicalPath());
             inv.setFrontal(frontal.getCanonicalPath());
             inv.setTrasera(trasera.getCanonicalPath());
             return inv;
         } catch (IOException ex) {
             throw new RuntimeException(ex); 
         }
     }
     
     private void generaSVGImagenDamage(ShapeView fondo, List<DamageDetailsVB> danios, File outputfile, int width, int height) {
         // Get a DOMImplementation.
         DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
 
         // Create an instance of org.w3c.dom.Document.
         String svgNS = "http://www.w3.org/2000/svg";
         Document document = domImpl.createDocument(svgNS, "svg", null);
 
         // Create an instance of the SVG Generator.
         SVGGraphics2D g2 = new SVGGraphics2D(document);
         // pintar.
         g2.setSVGCanvasSize(new java.awt.Dimension(width, height));
         Point contexto = new Point((width - fondo.getShapeWidth()) / 2, (height - fondo.getShapeHeight()) / 2);
         g2.setColor(Color.WHITE);
         g2.fillRect(0, 0, width, height);
         AffineTransform saveXform = g2.getTransform();
         AffineTransform toCenterAt = new AffineTransform();
         toCenterAt.translate(contexto.getX(), contexto.getY());
         g2.transform(toCenterAt);
         fondo.paint(g2);
         g2.setTransform(saveXform);
         for (DamageDetailsVB x: danios) {
             DamageDetailGraphicsView obj = new DamageDetailGraphicsView();
             obj.setFontSize(esquemaFontSize);
             obj.setPosicion(new Point(x.getX(), x.getY()));
             obj.setContexto(contexto);
             if (x.getX() <= fondo.getShapeWidth() / 2) {
                 if (x.getY() <= fondo.getShapeHeight() / 2) {
                     obj.setOrientacion(DamageDetailGraphicsView.SuperiorIzquierda);
                 } else {
                     obj.setOrientacion(DamageDetailGraphicsView.InferiorIzquierda);
                 }
             } else {
                 if (x.getY() <= fondo.getShapeHeight() / 2) {
                     obj.setOrientacion(DamageDetailGraphicsView.SuperiorDerecha);
                 } else {
                     obj.setOrientacion(DamageDetailGraphicsView.InferiorDerecha);
                 }
             }
             obj.setTexto(x.toString());
             obj.paint(g2);
         }
         try {   
             g2.stream(outputfile.getCanonicalPath());
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         }
     }
     
     private void generaImagenDamage(ShapeView fondo, List<DamageDetailsVB> danios, File outputfile, int width, int height) {
          try {
             Point contexto = new Point((width - fondo.getShapeWidth()) / 2,
                     (height - fondo.getShapeHeight()) / 2);
             BufferedImage off_Image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
             Graphics2D g2 = off_Image.createGraphics();
             g2.setColor(Color.WHITE);
             g2.fillRect(0, 0, width, height);
             AffineTransform saveXform = g2.getTransform();
             AffineTransform toCenterAt = new AffineTransform();
             toCenterAt.translate(contexto.getX(), contexto.getY());
             g2.transform(toCenterAt);
             fondo.paint(g2);
             g2.setTransform(saveXform);
             for (DamageDetailsVB x: danios) {
                 DamageDetailGraphicsView obj = new DamageDetailGraphicsView();
                 obj.setPosicion(new Point(x.getX(), x.getY()));
                 obj.setContexto(contexto);
                 if (x.getX() <= fondo.getShapeWidth() / 2) {
                     if (x.getY() <= fondo.getShapeHeight() / 2) {
                         obj.setOrientacion(DamageDetailGraphicsView.SuperiorIzquierda);
                     } else {
                         obj.setOrientacion(DamageDetailGraphicsView.InferiorIzquierda);
                     }
                 } else {
                     if (x.getY() <= fondo.getShapeHeight() / 2) {
                         obj.setOrientacion(DamageDetailGraphicsView.SuperiorDerecha);
                     } else {
                         obj.setOrientacion(DamageDetailGraphicsView.InferiorDerecha);
                     }
                 }
                 obj.setTexto(x.toString());
                 obj.paint(g2);
             }
             saveJPG(off_Image, outputfile);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     
     private String generaImagenCombustibleSVG(double porcentaje) {
         try {
             // Get a DOMImplementation.
             DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
 
             // Create an instance of org.w3c.dom.Document.
             String svgNS = "http://www.w3.org/2000/svg";
             Document document = domImpl.createDocument(svgNS, "svg", null);
 
             // Create an instance of the SVG Generator.
             SVGGraphics2D g2 = new SVGGraphics2D(document);
             int width = 186;
             int height = 15;
             g2.setSVGCanvasSize(new java.awt.Dimension(width, height));
             IndicadorBarraGraphicsView view = new IndicadorBarraGraphicsView();
             view.setWidthBar(width);
             view.setHeightBar(height);
             view.setPorcentaje(porcentaje);
             g2.setColor(Color.WHITE);
             g2.fillRect(0, 0, width, height);
             view.paint(g2);
             File file = new File("data/nivelCombustible.svg");
             try {   
                 g2.stream(file.getCanonicalPath());
             } catch (IOException ex) {
                 throw new RuntimeException(ex);
             }
             return file.getCanonicalPath();
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         }
     }
     
     private String generaImagenCombustible(double porcentaje) {
         try {
             int width = 186 * 3;
             int height = 15 * 3;
             IndicadorBarraGraphicsView view = new IndicadorBarraGraphicsView();
             view.setWidthBar(width);
             view.setHeightBar(height);
             view.setPorcentaje(porcentaje);
             BufferedImage off_Image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
             Graphics2D g2 = off_Image.createGraphics();
             g2.setColor(Color.WHITE);
             g2.fillRect(0, 0, width, height);
             view.paint(g2);
             File file = new File("data/nivelCombustible.jpg");
             saveJPG(off_Image, file);
             return file.getCanonicalPath();
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         }
     }
     
     private void saveJPG(BufferedImage img, File file) {
         ImageWriter writer = null;
         FileImageOutputStream output = null;
         try {
             writer = ImageIO.getImageWritersByFormatName("jpeg").next();
             ImageWriteParam param = writer.getDefaultWriteParam();
             param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
             param.setCompressionQuality(1);
             output = new FileImageOutputStream(file);
             writer.setOutput(output);
             IIOImage iioImage = new IIOImage(img, null, null);
             writer.write(null, iioImage, param);
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         } finally {
             try {
                 if (writer != null) {
                     writer.dispose();
                 }
                 if (output != null) {
                     output.close();
                 }
             } catch (IOException ex) {
                 throw new RuntimeException(ex);
             }
         }
     }
 }
