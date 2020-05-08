 /*
  * Copyright (C) 2005-2008 Michael Keith, ATNF CSIRO
  * 
  * email: mkeith@pulsarastronomy.net
  * www  : www.pulsarastronomy.net
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package bookkeepr.jettyhandlers;
 
 import bookkeepr.BookKeepr;
 import bookkeepr.BookKeeprException;
 import bookkeepr.managers.ObservationManager;
 import bookkeepr.managers.TypeIdManager;
 import bookkeepr.xml.XMLAble;
 import bookkeepr.xml.XMLReader;
 import bookkeepr.xml.XMLWriter;
 import bookkeepr.xmlable.CreatePointingsRequest;
 import bookkeepr.xmlable.DatabaseManager;
 import bookkeepr.xmlable.PointingSelectRequest;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.mortbay.jetty.Request;
 import org.mortbay.jetty.handler.AbstractHandler;
 import org.xml.sax.SAXException;
 import bookkeepr.xmlable.Configuration;
 import bookkeepr.xmlable.CreateScheduleRequest;
 import bookkeepr.xmlable.Receiver;
 import bookkeepr.xmlable.Telescope;
 import bookkeepr.xmlable.Session;
 
 import bookkeepr.managers.observationdatabase.PsrXMLManager.PointingNotFoundException;
 import bookkeepr.xml.IdAble;
 import bookkeepr.xmlable.BackgroundedTask;
 import bookkeepr.xmlable.ExtendedPointing;
 import bookkeepr.xmlable.Pointing;
 import bookkeepr.xmlable.PointingIndex;
 import bookkeepr.xmlable.Psrxml;
 import bookkeepr.xmlable.PsrxmlIndex;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.zip.GZIPOutputStream;
 import javax.imageio.ImageIO;
 
 /**
  *
  * @author kei041
  */
 public class ObservationHandler extends AbstractHandler {
 
     private static Pattern regex = Pattern.compile("/");
     BookKeepr bookkeepr;
     DatabaseManager manager;
     ObservationManager observationManager;
     HashMap<Integer, BufferedImage> images = new HashMap<Integer, BufferedImage>();
     HashMap<Integer, Date> imageDates = new HashMap<Integer, Date>();
     ArrayList<double[][][]> tileParams = null;
 
     public ObservationHandler(BookKeepr bookkeepr, DatabaseManager manager, ObservationManager observationManager) {
         this.bookkeepr = bookkeepr;
         this.manager = manager;
         this.observationManager = observationManager;
     }
 
     private void initTileParams() {
 
 
         int maxdepth = 6;
         int tilesize = 128;
 
         File cachefile = new File(observationManager.getDbManager().getRootPath() + File.separator + "skyview." + maxdepth + "." + tilesize + ".cache");
 
         try {
 
             this.tileParams = new ArrayList(maxdepth);
             if (cachefile.canRead()) {
                 Logger.getLogger(ObservationHandler.class.getName()).log(Level.INFO, "loading SkyView cace file " + cachefile.getPath());
 
                 BufferedReader reader = new BufferedReader(new FileReader(cachefile));
                 for (int i = 0; i <= maxdepth; i++) {
                     int level = (int) (Math.pow(2, i));
                     double[][][] par = new double[level][level][3];
 
                     for (int row = 0; row < level; row++) {
                         for (int col = 0; col < level; col++) {
                             String line = reader.readLine();
                             String[] elems = line.split("\\s+");
                             par[row][col][0] = Double.parseDouble(elems[3]);
                             par[row][col][1] = Double.parseDouble(elems[4]);
                             par[row][col][2] = 1.5 * 180.0 / level; //Double.parseDouble(elems[5]);
 
                         }
                     }
                     this.tileParams.add(i, par);
                 }
                 reader.close();
                 return;
 
             }
 
         } catch (Exception e) {
             Logger.getLogger(ObservationHandler.class.getName()).log(Level.WARNING, "Error Loading SkyView cache file", e);
         }
 
         this.tileParams = new ArrayList(maxdepth);
         try {
             PrintStream out = new PrintStream(new FileOutputStream(cachefile));
             for (int i = 0; i <= maxdepth; i++) {
                 int level = (int) (Math.pow(2, i));
                 double[][][] par = new double[level][level][3];
                 Logger.getLogger(ObservationHandler.class.getName()).log(Level.INFO, "Intialising SkyView level " + i + " (" + level + ")");
 
                 for (int row = 0; row < level; row++) {
                     for (int col = 0; col < level; col++) {
                         double[] vvv = observationManager.getParamsOfImgSquare(tilesize, tilesize, level, level, col * tilesize, row * tilesize);
                         par[row][col][0] = vvv[0];
                         par[row][col][1] = vvv[1];
                         par[row][col][2] = 1.5 * 180.0 / level;
                         out.printf("%d %d %d %f %f %f\n", level, row, col, vvv[0], vvv[1], 1.5 * 180.0 / level);
                     }
                 }
 
                 this.tileParams.add(i, par);
             }
             out.close();
             Logger.getLogger(ObservationHandler.class.getName()).log(Level.INFO, "SkyView initialisation complete.");
 
         } catch (Exception e) {
             Logger.getLogger(ObservationHandler.class.getName()).log(Level.WARNING, "Error creating SkyView cache file", e);
 
         }
 
     }
 
     /**
      * This handler deals with modifications and 'complex' requests to the obseration part of the database.
      * All the work is done by an {@link ObservationManager} or it's subobjects.
      * 
      * This deals with
      * <ul>
      * <li>Getting pointings/observations that match criteria</li>
      * <li>Generating an observing schedule based on existing unobserved pointings</li>
      * <li>Creating new pointings</li>
      * <li>Marking pointings to be in need of re-observation</li>
      * </ul>
      * 
      * 
      * @param path
      * @param request
      * @param response
      * @param dispatch
      * @throws java.io.IOException
      * @throws javax.servlet.ServletException
      */
     public void handle(String path, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
 
         if (path.startsWith("/obs/")) {
             ((Request) request).setHandled(true);
             if (path.equals("/obs/manager")) {
                 if (request.getMethod().equals("POST")) {
                     try {
                         XMLAble xmlable = (XMLAble) XMLReader.read(request.getInputStream());
                         if (xmlable instanceof PointingSelectRequest) {
                             handle((PointingSelectRequest) xmlable, request, response);
                             return;
                         } else if (xmlable instanceof CreatePointingsRequest) {
                             handle((CreatePointingsRequest) xmlable, response);
                             return;
                         } else if (xmlable instanceof CreateScheduleRequest) {
                             handle((CreateScheduleRequest) xmlable, response);
                             return;
                         } else if (xmlable instanceof Psrxml) {
                             //XMLWriter.write(new FileOutputStream("test.psrxml"), xmlable);
                             handle((Psrxml) xmlable, response);
                             return;
                         } else if (xmlable instanceof PsrxmlIndex) {
                             // submit psrxmls as an index, JUST FOR TESTING!
                             handle((PsrxmlIndex) xmlable, response);
 
                             return;
                         } else {
                             response.sendError(400, "Submitted request was not understood");
                             return;
                         }
 
                     } catch (SAXException ex) {
                         response.sendError(400, "Submitted request was malformed\nMessage was '" + ex.getMessage() + "'");
                         Logger.getLogger(ObservationHandler.class.getName()).log(Level.INFO, "Recieved malformed request");
                         return;
                     }
 
                 } else {
                     response.sendError(400, "Submitted request was not understood: Not a POST message");
                     Logger.getLogger(ObservationHandler.class.getName()).log(Level.INFO, "Recieved malformed request");
                     return;
                 }
 
             } else if (path.equals("/obs/querypsrxml")) {
                 if (request.getMethod().equals("POST")) {
                     try {
                         XMLAble xmlable = (XMLAble) XMLReader.read(request.getInputStream());
                         if (xmlable instanceof Psrxml) {
                             //XMLWriter.write(new FileOutputStream("test.psrxml"), xmlable);
                             this.queryPsrxml((Psrxml) xmlable, response);
                             return;
                         } else {
                             response.sendError(400, "Submitted request was not understood");
                             return;
                         }
 
                     } catch (SAXException ex) {
                         response.sendError(400, "Submitted request was malformed\nMessage was '" + ex.getMessage() + "'");
                         Logger.getLogger(ObservationHandler.class.getName()).log(Level.INFO, "Recieved malformed request");
                         return;
                     }
 
                 } else {
                     response.sendError(400, "Submitted request was not understood: Not a POST message");
                     Logger.getLogger(ObservationHandler.class.getName()).log(Level.INFO, "Recieved malformed request");
                     return;
                 }
 
             } else if (path.startsWith("/obs/eptg")) {
 
                 ((Request) request).setHandled(true);
                 String[] elems = regex.split(path.substring(2));
                 if (request.getMethod().equals("GET") && elems.length > 1) {
                     try {
                         String idStr = elems[2];
                         long id = Long.parseLong(idStr, 16);
                         IdAble idable = manager.getById(id);
                         Configuration conf = null;
                         if (idable != null && idable instanceof Pointing) {
                             Pointing ptg = ((Pointing) idable);
 
                             if (conf == null || conf.getId() != ptg.getConfigurationId()) {
                                 conf = (Configuration) this.manager.getById(ptg.getConfigurationId());
                             }
 
                             ExtendedPointing eptg = new ExtendedPointing(ptg);
                             eptg.setObservations(this.observationManager.getObservations(ptg));
                             eptg.setScheduleLine(conf.getScheduleLine(ptg));
                             eptg.setTobs(conf.getTobs());
                             XMLWriter.write(response.getOutputStream(), eptg, true);
                             response.getOutputStream().close();
                         } else {
                             response.sendError(404, "Requested element not found");
                         }
                     } catch (NumberFormatException ex) {
                         response.sendError(400, "Request URL had a bad ID in it");
 
                     } finally {
                         return;
                     }
 
 
 
                 } else {
                     response.sendError(400, "Submitted request was not understood");
                     return;
                 }
 
             } else if (path.startsWith("/obs/map/")) {
                 if (path.startsWith("/obs/map/tiles/")) {
                     synchronized (this) {
                         if (tileParams == null) {
                             initTileParams();
                         }
                     }
                     String[] elems = path.substring(19).split("-|\\.png");
                     if (elems.length > 2) {
                         int nlev = Integer.parseInt(elems[0]);
                         int level = (int) (Math.pow(2, nlev));
                         //int level = Integer.parseInt(elems[0]) + 1;
                         int row = Integer.parseInt(elems[2]);
                         int col = Integer.parseInt(elems[1]);
                         if (col > level || row > level) {
                             //  response.sendError(404, "Requested element not found");
                             return;
                         }
                         int tilesize = 128;
 //                        BufferedImage levelImage = null;
 //                        synchronized (images) {
 //                            Date now = new Date();
 //
 //                            levelImage = images.get(level);
 //                            if (levelImage == null || (now.getTime() - imageDates.get(level).getTime()) > 60000L) {
 //                                Logger.getLogger(ObservationHandler.class.getName()).log(Level.INFO, "Creating new map for zoom " + (level * tilesize));
 //                                levelImage = observationManager.makeImage(tilesize * level, tilesize * level);
 //                                images.put(level, levelImage);
 //                                imageDates.put(level, now);
 //                            }
 //                        }
 //                        BufferedImage img = new BufferedImage(tilesize, tilesize, BufferedImage.TYPE_INT_ARGB);
 //                        for (int x = 0; x < tilesize; x++) {
 //                            for (int y = 0; y < tilesize; y++) {
 //                                int rgb = new Color(0f, 0f, 0f, 0f).getRGB();
 //                                int xx = x + (col) * tilesize;
 //                                int yy = y + (row) * tilesize;
 //                                if (xx < levelImage.getWidth() && yy < levelImage.getHeight()) {
 //                                    rgb = levelImage.getRGB(xx, yy);
 //                                }
 //                                img.setRGB(x, y, rgb);
 //                            }
 //                        }
 
                         BufferedImage img;
                         img = observationManager.makeImage(this.tileParams.get(nlev)[row][col], tilesize, tilesize, level, level, col * tilesize, row * tilesize);
                         response.setContentType("image/png");
                         ImageIO.write(img, "PNG", response.getOutputStream());
                         response.getOutputStream().close();
 
                         return;
                     } else {
                         response.sendError(404, "Requested element not found");
                         return;
                     }
                 } else {
 
                     int h = 600;
                     int w = 800;
                     String[] elems = regex.split(path.substring(2));
                     if (elems.length > 2) {
                         String imgname = elems[2];
                         String[] parts = imgname.split("x|\\.png");
                         if (parts.length > 1) {
                             try {
                                 h = Integer.parseInt(parts[0]);
                                 w = Integer.parseInt(parts[1]);
                             } catch (NumberFormatException e) {
                             }
                         }
                     }
                     response.setContentType("image/png");
                     ImageIO.write(observationManager.makeImage(h, w), "PNG", response.getOutputStream());
                 }
             } else if (path.equals("/obs/create")) {
                 if (request.getMethod().equals("POST")) {
                     try {
                         XMLAble xmlable = (XMLAble) XMLReader.read(request.getInputStream());
 
                         if (xmlable instanceof Configuration || xmlable instanceof Telescope || xmlable instanceof Receiver) {
                             Session session = new Session();
                             this.manager.add((IdAble) xmlable, session);
                             this.manager.save(session);
 
                         } else {
                             response.sendError(400, "Submitted request was not understood");
                         }
 
                     } catch (BookKeeprException ex) {
                         Logger.getLogger(ObservationHandler.class.getName()).log(Level.WARNING, null, ex);
                         response.sendError(500, "Submitted could not be carried out\nMessage was '" + ex.getMessage() + "'");
                     } catch (SAXException ex) {
                         response.sendError(400, "Submitted request was malformed\nMessage was '" + ex.getMessage() + "'");
                         Logger.getLogger(ObservationHandler.class.getName()).log(Level.INFO, "Recieved malformed request");
                     }
 
                 } else {
                     response.sendError(400, "Submitted request was not understood");
 
                 }
 
             } else if (path.equals("/obs/status")) {
                 if (request.getMethod().equals("GET")) {
                 } else {
                     response.sendError(400, "Submitted request was not understood");
 
                 }
 
             } else if (path.equals("/obs/dumpptg")) {
                 response.setContentType("application/gzip");
                 List list = manager.getAllOfType(TypeIdManager.getTypeFromClass(Pointing.class));
                 PrintStream out = new PrintStream(new GZIPOutputStream(response.getOutputStream()));
                 for (Object o : list) {
                     Pointing ptg = (Pointing) o;
 //                    if(ptg.getToBeObserved())
                     out.printf("Ix%s\t%s\t%s\t%5.2f\t%5.2f\tCx%s\n", Long.toHexString(ptg.getId()), ptg.getGridId(), ptg.getCoordinate().toString(false), ptg.getCoordinate().getRA().toDegrees(), ptg.getCoordinate().getDec().toDegrees(), Long.toHexString(ptg.getConfigurationId()));
                 }
                 out.close();
 
             } else {
 
                 response.sendError(404, "Submitted request was to a non-existant url");
             }
         }
     }
 
     private void handle(Psrxml request, HttpServletResponse response) {
         Logger.getLogger(ObservationHandler.class.getName()).log(Level.FINE, "Received a PsrXML file for insertion.");
 
         try {
 
 
             try {
                 this.observationManager.addPsrXML(request);
             } catch (PointingNotFoundException ex) {
                 Logger.getLogger(ObservationHandler.class.getName()).log(Level.WARNING, "Grid ID issue with psrxml file. " + ex.getMessage(), ex);
                 response.sendError(500, ex.getMessage());
             } catch (BookKeeprException ex) {
                 Logger.getLogger(ObservationHandler.class.getName()).log(Level.WARNING, "Issue updating database", ex);
                 response.sendError(500, ex.getMessage());
             }
             XMLWriter.write(response.getOutputStream(), request);
 
 
         } catch (IOException ex) {
             Logger.getLogger(ObservationHandler.class.getName()).log(Level.WARNING, "IOException writing to client", ex);
         }
     }
     /*
      * This method is for testing only!
      * 
      * 
      */
 
     private void handle(PsrxmlIndex request, HttpServletResponse response) {
         Logger.getLogger(ObservationHandler.class.getName()).log(Level.FINE, "Received a PsrXML file for insertion.");
 
         try {
 
             Session session = new Session();
             for (Psrxml psrxml : request.getPsrxmlList()) {
                 try {
 
                     this.observationManager.addPsrXML(psrxml, session);
 
 
                 } catch (PointingNotFoundException ex) {
                     Logger.getLogger(ObservationHandler.class.getName()).log(Level.WARNING, "Grid ID issue with psrxml file. " + ex.getMessage(), ex);
                 //response.sendError(500, ex.getMessage());
 
                 }
 
 //            XMLWriter.write(response.getOutputStream(), request);
             }
             this.manager.save(session);
 
         } catch (BookKeeprException ex) {
             Logger.getLogger(ObservationHandler.class.getName()).log(Level.WARNING, "Issue updating database", ex);
             try {
                 response.sendError(500, ex.getMessage());
             } catch (IOException ex1) {
                 Logger.getLogger(ObservationHandler.class.getName()).log(Level.SEVERE, null, ex1);
             }
         }
     }
 
     private void handle(PointingSelectRequest request, HttpServletRequest httpreq, HttpServletResponse response) {
         Logger.getLogger(ObservationHandler.class.getName()).log(Level.FINE, "Processing a pointing search request");
         try {
 
             OutputStream out = response.getOutputStream();
             String hdr = httpreq.getHeader("Accept-Encoding");
             if (hdr != null && hdr.contains("gzip")) {
                 // if the host supports gzip encoding, gzip the output for quick transfer speed.
                 out = new GZIPOutputStream(out);
                 response.setHeader("Content-Encoding", "gzip");
             }
 
 
             PointingIndex idx = observationManager.getPointings(request);
             XMLWriter.write(out, idx);
             out.close();
         } catch (IOException ex) {
             Logger.getLogger(ObservationHandler.class.getName()).log(Level.WARNING, "IOException writing to client", ex);
         }
     }
 
     private void handle(final CreatePointingsRequest request,
             HttpServletResponse response) {
         Logger.getLogger(ObservationHandler.class.getName()).log(Level.FINE, "Processing a pointing CREATE request");
 
         final BackgroundedTask bgtask = new BackgroundedTask("CreatePointings");
 
         bgtask.setTarget(new Runnable() {
 
             public void run() {
                 observationManager.createNewPointings(request, bgtask.getWriter());
             }
         });
 
         bookkeepr.getBackgroundTaskRunner().offer(bgtask);
         StringBuffer buf = new StringBuffer();
         Formatter formatter = new Formatter(buf);
         formatter.format("%s/%s/%d", manager.getExternalUrl(), "tasks", bgtask.getId());
         response.setStatus(303);
         response.addHeader("Location", buf.toString());
 
 
     }
 
     private void handle(CreateScheduleRequest request, HttpServletResponse response) {
 
         try {
             try {
                 List<String> text = this.observationManager.createSchedule(request);
                 if (text != null) {
                     response.setContentType("text/plain");
                     PrintStream out = new PrintStream(response.getOutputStream());
                     for (String line : text) {
                         out.println(line);
                     }
 
                 }
             } catch (RuntimeException ex) {
                 response.sendError(500, "Could not create schedule because: " + ex.getMessage());
                 Logger.getLogger(ObservationHandler.class.getName()).log(Level.WARNING, "Exception creating schedule", ex);
             }
 
         } catch (IOException ex) {
             Logger.getLogger(ObservationHandler.class.getName()).log(Level.WARNING, "IOException writing to client", ex);
         }
 
 
     }
 
     private void queryPsrxml(Psrxml request, HttpServletResponse response) {
        Logger.getLogger(ObservationHandler.class.getName()).log(Level.FINE, "Received a PsrXML file for insertion.");
         Psrxml out = null;
         try {
 
 
             try {
                 out = this.observationManager.queryPsrXML(request);
             } catch (PointingNotFoundException ex) {
                 Logger.getLogger(ObservationHandler.class.getName()).log(Level.WARNING, "Grid ID issue with psrxml file. " + ex.getMessage(), ex);
                 response.sendError(500, ex.getMessage());
             } catch (BookKeeprException ex) {
                 Logger.getLogger(ObservationHandler.class.getName()).log(Level.WARNING, "Issue updating database", ex);
                 response.sendError(500, ex.getMessage());
             }
             XMLWriter.write(response.getOutputStream(), out);
 
 
         } catch (IOException ex) {
             Logger.getLogger(ObservationHandler.class.getName()).log(Level.WARNING, "IOException writing to client", ex);
         }
     }
 }
