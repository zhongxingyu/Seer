 package pro.kornev.kcar.cop.services.sensors;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 
 import java.nio.ByteBuffer;
 
 import pro.kornev.kcar.cop.services.CopService;
 import pro.kornev.kcar.cop.services.network.NetworkListener;
 import pro.kornev.kcar.protocol.Data;
 import pro.kornev.kcar.protocol.Protocol;
 
 /**
  *
  */
public final class LocationService implements NetworkListener, LocationListener {
     private final LocationManager locationManager;
     private final CopService copService;
     private final double[] location = {0, 0};
 
     public LocationService(CopService copService) {
         this.copService = copService;
         locationManager= (LocationManager) copService.getSystemService(Context.LOCATION_SERVICE);
     }
 
     @Override
     public void onDataReceived(Data data) {
         if (data.cmd != Protocol.Cmd.sensGps()) {
             return;
         }
         ByteBuffer bb = ByteBuffer.allocate(Double.SIZE / 8 * 2);
         bb.putDouble(location[0]);
         bb.putDouble(location[1]);
         byte[] buf = bb.array();
 
         data.type = Protocol.arrayType();
         data.aSize = buf.length;
         data.aData = buf;
         write(data);
     }
 
     @Override
     public void onLocationChanged(Location location) {
         this.location[0] = location.getLatitude();
         this.location[1] = location.getLongitude();
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {
 
     }
 
     @Override
     public void onProviderEnabled(String provider) {
 
     }
 
     @Override
     public void onProviderDisabled(String provider) {
 
     }
 
     public void start() {
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
         locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
     }
 
     public void stop() {
         locationManager.removeUpdates(this);
     }
 
     private void write (Data data) {
         if (copService.getNetworkService() == null) {
             return;
         }
         copService.getNetworkService().write(data);
     }
 }
