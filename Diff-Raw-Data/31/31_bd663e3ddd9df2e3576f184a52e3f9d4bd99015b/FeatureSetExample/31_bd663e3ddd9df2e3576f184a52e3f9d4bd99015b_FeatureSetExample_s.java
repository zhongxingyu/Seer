 package net.beadsproject.beads.analysis;
 
 import java.io.File;
 import net.beadsproject.beads.analysis.featureextractors.FFT;
 import net.beadsproject.beads.analysis.featureextractors.MelSpectrum;
 import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
 import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
 import net.beadsproject.beads.core.AudioContext;
 import net.beadsproject.beads.core.Bead;
 import net.beadsproject.beads.data.Sample;
 import net.beadsproject.beads.data.SampleManager;
 import net.beadsproject.beads.ugens.SamplePlayer;
 
 public class FeatureSetExample {
 
 	public static void main(String[] args) {
 		final Sample s = SampleManager.sample("audio/1234.aif");
 		System.out.println(s.getFileName());
 		System.out.println("Processing");
 		final AudioContext ac = new AudioContext();
 		SamplePlayer sp = new SamplePlayer(ac, s);
 		//setup analysis
 		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
 		FFT fft = new FFT();
 		PowerSpectrum ps = new PowerSpectrum();
 		MelSpectrum ms = new MelSpectrum(ac.getSampleRate(), 10);
 		//set up chain
 		sfs.addListener(fft);
 		fft.addListener(ps);
 		ps.addListener(ms);
 		//link in
 		ac.out.addDependent(sfs);
 		sfs.addInput(sp);
 		//set up recorder
 		final FeatureTrack ft = new FeatureTrack();
 		sfs.addSegmentListener(ft);
 		ft.addFeatureExtractor(ms);
 		//prepare ending
 		sp.setKillListener(new Bead() {
 			public void messageReceived(Bead message) {
 				System.out.println("Done");
 				ac.stop();
 				FeatureSet fs = new FeatureSet();
 				fs.add("features", ft);
 				fs.write(s.getFileName() + ".features");
 			}
 		});
 		ac.runNonRealTime();
 		//once this stops, look at the file
 		File featureFile = new File(s.getFileName() + ".features");
 		FeatureSet fs = new FeatureSet(featureFile);
 		
 		FeatureTrack myTrack = fs.get("features");
 		
		double testTimeMS = 10000;
 		FeatureFrame ff = myTrack.getFrameAt(testTimeMS);
 		System.out.println(ff);
 		
 		featureFile.delete();
 	}
 }
