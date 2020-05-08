 package com.imolatech.retina.kinect.serializer;
 
 import java.util.HashMap;
 
 import org.OpenNI.Point3D;
 import org.OpenNI.SkeletonJoint;
 import org.OpenNI.SkeletonJointPosition;
 import org.junit.Test;
 import static org.hamcrest.Matchers.greaterThanOrEqualTo;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class TrackedUserSerializerTest {
 	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> usersSkeletons;
 	
 	
 	@Test
 	public void toJson() {
 		buildUsersSkeletons();
 		TrackedUsersSerializer serializer = new TrackedUsersSerializer(usersSkeletons);
 		String json = serializer.toJson();
 		System.out.println(json);
		String expected = "{\"users\":[{\"id\":1,\"active\":false,\"joints\":[{\"name\":\"LEFT_FOOT\"," +
				"\"position\":{\"X\":120.0,\"Y\":120.0,\"Z\":120.0},\"confidence\":0.8}," +
				"{\"name\":\"HEAD\",\"position\":{\"X\":20.0,\"Y\":20.0,\"Z\":20.0},\"confidence\":0.8}]}," +
				"{\"id\":2,\"active\":false,\"joints\":[{\"name\":\"LEFT_FOOT\"," +
				"\"position\":{\"X\":120.0,\"Y\":120.0,\"Z\":120.0},\"confidence\":0.8}," +
				"{\"name\":\"HEAD\",\"position\":{\"X\":20.0,\"Y\":20.0,\"Z\":20.0},\"confidence\":0.8}]}]," +
				"\"type\":\"TRACKED_USERS\",";
 		
 		assertThat(json.indexOf(expected), is(greaterThanOrEqualTo(0)));
		
 	}
 	
 	private void buildUsersSkeletons() {
 		usersSkeletons = new HashMap<Integer, HashMap<SkeletonJoint,SkeletonJointPosition>>();
 		HashMap<SkeletonJoint,SkeletonJointPosition> joints = new HashMap<SkeletonJoint, SkeletonJointPosition>();
 		joints.put(SkeletonJoint.HEAD, buildJointPosition(20.0f, 20.0f, 20.0f));
 		joints.put(SkeletonJoint.LEFT_FOOT, buildJointPosition(120.0f, 120.0f, 120.0f));
 		usersSkeletons.put(1, joints);
 		usersSkeletons.put(2, joints);
 	}
 	
 	private SkeletonJointPosition buildJointPosition(float x, float y, float z) {
 		Point3D p = new Point3D(x, y, z);
 		SkeletonJointPosition position = new SkeletonJointPosition(p, 0.8f); 
 		return position;
 	}
 }
