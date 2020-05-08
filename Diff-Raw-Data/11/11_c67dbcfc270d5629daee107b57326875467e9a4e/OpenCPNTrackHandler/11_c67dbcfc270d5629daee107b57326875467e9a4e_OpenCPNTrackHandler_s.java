 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.vesalainen.mailblog;
 
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.GeoPt;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Transaction;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.vesalainen.gpx.TrackHandler;
 import static org.vesalainen.mailblog.BlogConstants.SouthWestProperty;
 import org.w3c.dom.Element;
 
 /**
  * @author Timo Vesalainen
  */
 public class OpenCPNTrackHandler implements TrackHandler, BlogConstants
 {
     private DS ds;
     private Entity track;
     private Entity trackSeq;
     private List<Entity> entities = new ArrayList<>();
     private LatLonAltBox box = new LatLonAltBox();
 
     public OpenCPNTrackHandler(DS ds)
     {
         this.ds = ds;
     }
     
     @Override
     public boolean startTrack(Collection<Object> extensions)
     {
         String guid = getGuid(extensions);
         if (guid != null)
         {
             Key trackKey = ds.getTrackKey(guid);
             try
             {
                 ds.get(trackKey);
                 ds.deleteWithChilds(trackKey);
             }
             catch (EntityNotFoundException ex)
             {
             }
             track = new Entity(trackKey);
             ds.put(track);
             return true;
         }
         else
         {
             System.err.println("no guid! Skipping track...");
             return false;
         }
     }
 
     @Override
     public void endTrack()
     {
         Transaction tr = ds.beginTransaction();
         try
         {
             ds.put(entities);
             tr.commit();
         }
         finally
         {
             entities.clear();
             if (tr.isActive())
             {
                 tr.rollback();
             }
         }
     }
 
     @Override
     public void startTrackSeq()
     {
         trackSeq = new Entity(TrackSeqKind, track.getKey());
        ds.put(trackSeq);
     }
 
     @Override
     public void endTrackSeq()
     {
         trackSeq.setProperty(SouthWestProperty, box.getSouthWest());
         trackSeq.setProperty(NorthEastProperty, box.getNorthEast());
         box.clear();
         trackSeq = null;
     }
 
     @Override
     public void trackPoint(double latitude, double longitude, long time)
     {
         Entity point = new Entity(TrackPointKind, time, trackSeq.getKey());
         point.setProperty(LocationProperty, new GeoPt((float)latitude, (float)longitude));
         ds.put(point);
     }
     public static String getGuid(Collection<Object> extensions)
     {
         for (Object ob : extensions)
         {
             if (ob instanceof Element)
             {
                 Element el = (Element) ob;
                 if (
                         "http://www.opencpn.org".equals(el.getNamespaceURI()) &&
                         "guid".equals(el.getLocalName())
                         )
                 {
                     return el.getTextContent();
                 }
             }
         }
         return null;
     }
 
 }
