 package uk.co.unclealex.music.inject;
 
 import java.util.concurrent.ExecutorService;
 
 import uk.co.unclealex.music.Device;
 import uk.co.unclealex.music.DeviceService;
 import uk.co.unclealex.music.FileService;
 import uk.co.unclealex.music.PlaylistService;
 import uk.co.unclealex.music.sync.DeviceSynchroniser;
 import uk.co.unclealex.music.sync.Synchroniser;
 import uk.co.unclealex.music.sync.SynchroniserFactory;
 import uk.co.unclealex.process.ProcessService;
 
 import com.google.inject.Provider;
 
 public class GuiceSynchroniserFactory implements SynchroniserFactory {
 
 	private Provider<ExecutorService> i_executorServiceProvider;
 	private Provider<DeviceService> i_deviceServiceProvider;
 	private Provider<FileService> i_fileServiceProvider;
 	private Provider<ProcessService> i_processServiceProvider;
 	private Provider<PlaylistService> i_playlistServiceProvider;
 	
 	@Override
 	public Synchroniser createSynchroniser(Device device) {
 		return new DeviceSynchroniser(
 			getExecutorServiceProvider().get(), getDeviceServiceProvider().get(),
 			getFileServiceProvider().get(), getProcessServiceProvider().get(), getPlaylistServiceProvider().get(), device);
 	}
 
 	public Provider<ExecutorService> getExecutorServiceProvider() {
   	return i_executorServiceProvider;
   }
 
 	public Provider<DeviceService> getDeviceServiceProvider() {
   	return i_deviceServiceProvider;
   }
 
 	public Provider<FileService> getFileServiceProvider() {
   	return i_fileServiceProvider;
   }
 
 	public Provider<ProcessService> getProcessServiceProvider() {
   	return i_processServiceProvider;
   }
 
 	public Provider<PlaylistService> getPlaylistServiceProvider() {
   	return i_playlistServiceProvider;
   }
 }
