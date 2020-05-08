 package org.rhok.hh.hackingautism.mooddetector;
 
 import java.io.File;
 import java.util.List;
 
 import org.rhok.hh.hackingautism.mooddetector.model.Mood;
 
 import com.github.mhendred.face4j.DefaultFaceClient;
 import com.github.mhendred.face4j.FaceClient;
 import com.github.mhendred.face4j.model.Face;
 import com.github.mhendred.face4j.model.Photo;
 
 public class MoodDetector {
 
 	private static final String API_KEY = "cc592f20aae5983d377d6a438c75393c";
 	private static final String API_SECRET = "f899a88bb6324544d125cb655c6d2978";
 
 	public Mood detectMood(final File faceImageFile) {
		System.out.println("XXXXXXXXXXXX: " + faceImageFile);
 		final FaceClient client = new DefaultFaceClient(API_KEY, API_SECRET);
 		final Photo photo;
 		try {
 			photo = client.detect(faceImageFile);
 		} catch (final Exception e) {
			e.printStackTrace();
 			return null;
 		}
 		if (photo == null) {
 			return null;
 		}
 		final List<Face> faces = photo.getFaces();
 		if (faces == null || faces.size() != 1) {
 			return null;
 		}
 		final Face face = faces.get(0);
 		final String moodStr = face.getMood();
 		System.out.println("moodStr: " + moodStr);
 		return null;
 	}
 }
