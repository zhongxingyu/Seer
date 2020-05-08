 package uk.co.unclealex.music.inject;
 
 import java.io.File;
 import java.lang.annotation.Annotation;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Map;
 import java.util.SortedSet;
 
 import uk.co.unclealex.music.ArtistFixingService;
 import uk.co.unclealex.music.ArtistFixingServiceImpl;
 import uk.co.unclealex.music.Device;
 import uk.co.unclealex.music.DeviceService;
 import uk.co.unclealex.music.DeviceServiceImpl;
 import uk.co.unclealex.music.Encoding;
 import uk.co.unclealex.music.FileService;
 import uk.co.unclealex.music.FileServiceImpl;
 import uk.co.unclealex.music.FileSystemDevice;
 import uk.co.unclealex.music.IpodDevice;
 import uk.co.unclealex.music.LatinService;
 import uk.co.unclealex.music.LatinServiceImpl;
 import uk.co.unclealex.music.MtpDevice;
 import uk.co.unclealex.music.PlaylistService;
 import uk.co.unclealex.music.PlaylistServiceImpl;
 import uk.co.unclealex.music.covers.AmazonArtworkSearchingService;
 import uk.co.unclealex.music.covers.ArtworkManager;
 import uk.co.unclealex.music.covers.ArtworkSearchingService;
 import uk.co.unclealex.music.covers.FlacJAudioTaggerArtworkManager;
 import uk.co.unclealex.music.covers.Mp3JAudioTaggerArtworkManager;
 import uk.co.unclealex.music.covers.SignedRequestsService;
 import uk.co.unclealex.music.covers.SignedRequestsServiceImpl;
 import uk.co.unclealex.music.encoding.ArtworkUpdatingService;
 import uk.co.unclealex.music.encoding.ArtworkUpdatingServiceImpl;
 import uk.co.unclealex.music.encoding.EncodingService;
 import uk.co.unclealex.music.encoding.EncodingServiceImpl;
 import uk.co.unclealex.music.encoding.FileFixingService;
 import uk.co.unclealex.music.encoding.FileFixingServiceImpl;
 import uk.co.unclealex.music.encoding.ImageService;
 import uk.co.unclealex.music.encoding.ImageServiceImpl;
 import uk.co.unclealex.music.encoding.ImportService;
 import uk.co.unclealex.music.encoding.ImportServiceImpl;
 import uk.co.unclealex.music.encoding.RenamingService;
 import uk.co.unclealex.music.encoding.RenamingServiceImpl;
 import uk.co.unclealex.music.encoding.SingleEncodingService;
 import uk.co.unclealex.music.encoding.SingleEncodingServiceImpl;
 import uk.co.unclealex.music.sync.DeviceSynchroniser;
 import uk.co.unclealex.music.sync.Synchroniser;
 import uk.co.unclealex.music.sync.SynchroniserFactory;
 import uk.co.unclealex.music.sync.SynchroniserService;
 import uk.co.unclealex.music.sync.SynchroniserServiceImpl;
 
 import com.google.common.collect.ImmutableSortedSet;
 import com.google.inject.AbstractModule;
 import com.google.inject.Module;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.google.inject.assistedinject.FactoryModuleBuilder;
 
 public class MusicModule extends AbstractModule implements Module {
 
 	@Override
 	protected void configure() {
 		bindServices();
 		bindValues();
 		bindSynchronisers();
 		bindArtwork();
 	}
 
 	protected void bindServices() {
 		bind(LatinService.class).to(LatinServiceImpl.class);
 		bind(DeviceService.class).to(DeviceServiceImpl.class);
 		bind(FileService.class).to(FileServiceImpl.class);
 		bind(SynchroniserService.class).to(SynchroniserServiceImpl.class);
 		bind(ArtworkSearchingService.class).to(AmazonArtworkSearchingService.class);
 		bind(ArtworkUpdatingService.class).to(ArtworkUpdatingServiceImpl.class);
 		bind(SingleEncodingService.class).to(SingleEncodingServiceImpl.class);
 		bind(ImportService.class).to(ImportServiceImpl.class);
 		bind(ImageService.class).to(ImageServiceImpl.class);
 		bind(FileFixingService.class).to(FileFixingServiceImpl.class);
 		bind(RenamingService.class).to(RenamingServiceImpl.class);
 		bind(ArtistFixingService.class).to(ArtistFixingServiceImpl.class);
 		bind(EncodingService.class).to(EncodingServiceImpl.class);
 		bind(PlaylistService.class).to(PlaylistServiceImpl.class);
 		bind(SignedRequestsService.class).to(SignedRequestsServiceImpl.class).in(Singleton.class);
 	}
 
 	protected void bindValues() {
 		bindDirectory(DevicesDirectory.class, "devices");
 		bindDirectory(EncodedDirectory.class, "encoded");
 		bindDirectory(FlacDirectory.class, "flac");
 		bindDirectory(PlaylistsDirectory.class, "playlists");
 		bindConstant().annotatedWith(MaximumThreads.class).to(4);
 		bindConstant().annotatedWith(AwsEndpoint.class).to("ecs.amazonaws.co.uk");
 		bindConstant().annotatedWith(AwsAccessKeyId.class).to("1GK0A6KVNQEFVH5Q1Z82");
 		bindConstant().annotatedWith(AwsSecretKey.class).to("jq/BOXh4dOYXsso3jn/N8FGF7/40YCPHmTyas1ZF");
 		
 		bind(Encoding.class).annotatedWith(Mp3Encoding.class).toInstance(new Encoding("mp3"));
 		bind(Encoding.class).annotatedWith(OggEncoding.class).toInstance(new Encoding("ogg"));
 		
 	}
 
 	protected void bindDirectory(Class<? extends Annotation> annotationType, String directory) {
	  bind(File.class).annotatedWith(annotationType).toInstance(new File(new File("/mnt/mulitmedia"), directory));
   }
 
 	protected void bindArtwork() {
 		bind(ArtworkManager.class).annotatedWith(FlacArtworkManager.class).to(FlacJAudioTaggerArtworkManager.class);
 		bind(ArtworkManager.class).annotatedWith(Mp3ArtworkManager.class).to(Mp3JAudioTaggerArtworkManager.class);
 	}
 	
 	@Provides @EncodingArtworkManagers Map<Encoding, ArtworkManager> provideEncodingArtworkManagers(@Mp3Encoding Encoding mp3Encoding, @Mp3ArtworkManager ArtworkManager mp3ArtworkManager) {
 		return Collections.singletonMap(mp3Encoding, mp3ArtworkManager);
 	}
 	
 	protected void bindSynchronisers() {
 		install(new FactoryModuleBuilder().implement(Synchroniser.class, DeviceSynchroniser.class).build(SynchroniserFactory.class));
 	}
 
 	@Provides @AllDevices SortedSet<Device> provideAllDevices(@Mp3Encoding Encoding mp3Encoding, @OggEncoding Encoding oggEncoding) {
 		return sortedSetOf(
 			(Device) new IpodDevice("ipod", "alex", mp3Encoding, new File("/media/ALEX'S IPOD")),
 			(Device) new MtpDevice("creative zen", "alex", mp3Encoding),
 			(Device) new FileSystemDevice("walkman", "alex", mp3Encoding, new File("/media/WALKMAN"), "MUSIC"),
 			(Device) new IpodDevice("ipod", "trevor", mp3Encoding, new File("/media/TREVOR'S IP")),
 			(Device) new FileSystemDevice("iriver", "trevor", oggEncoding, new File("/media/disk"))
 		);
 	}
 	
 	@Provides @Encodings SortedSet<Encoding> provideEncodings(@Mp3Encoding Encoding mp3Encoding, @OggEncoding Encoding oggEncoding) {
 		return sortedSetOf(mp3Encoding, oggEncoding);
 	}
 	
 	protected <E> SortedSet<E> sortedSetOf(E... elements) {
 		return ImmutableSortedSet.copyOf(Arrays.asList(elements));
 	}
 }
