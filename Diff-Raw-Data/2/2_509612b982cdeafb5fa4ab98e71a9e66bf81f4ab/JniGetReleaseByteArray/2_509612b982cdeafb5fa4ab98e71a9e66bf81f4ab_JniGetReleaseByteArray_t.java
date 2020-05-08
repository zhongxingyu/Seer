 package com.luzi82.codeindex.android.testcase;
 
 import com.luzi82.codeindex.Case;
 import com.luzi82.codeindex.android.Jni;
 
 public class JniGetReleaseByteArray extends Case {
 
	public static final String DESCRIPTION = "JNI array access methods comparison";
 
 	public Object[] test_GetReleaseByteArrayElementsJNIABORT_speed_data() {
 		return getTestArraySize();
 	}
 
 	public void test_GetReleaseByteArrayElementsJNIABORT_speed(Object arg) {
 		final int ARRAY_SIZE = (Integer) arg;
 		final byte[] ary = new byte[ARRAY_SIZE];
 		long now = System.currentTimeMillis();
 		long startTime = now;
 		long endTime = startTime + TIME_LIMIT;
 		int done = 0;
 		while (now < endTime) {
 			Jni.loopGetReleaseByteArrayElementsJNIABORT(ary, LOOP_PER_TICK);
 			done += LOOP_PER_TICK;
 			now = System.currentTimeMillis();
 		}
 		long timeDone = now - startTime;
 		float donePerSec = (((float) done) / timeDone) * 1000;
 		msg(String.format("test_GetReleaseByteArrayElementsJNIABORT_speed: %d: %s times", ARRAY_SIZE, metricPrefix(donePerSec)));
 	}
 
 	public Object[] test_GetReleaseByteArrayElements0_speed_data() {
 		return getTestArraySize();
 	}
 
 	public void test_GetReleaseByteArrayElements0_speed(Object arg) {
 		final int ARRAY_SIZE = (Integer) arg;
 		final byte[] ary = new byte[ARRAY_SIZE];
 		long now = System.currentTimeMillis();
 		long startTime = now;
 		long endTime = startTime + TIME_LIMIT;
 		int done = 0;
 		while (now < endTime) {
 			Jni.loopGetReleaseByteArrayElements0(ary, LOOP_PER_TICK);
 			done += LOOP_PER_TICK;
 			now = System.currentTimeMillis();
 		}
 		long timeDone = now - startTime;
 		float donePerSec = (((float) done) / timeDone) * 1000;
 		msg(String.format("test_GetReleaseByteArrayElements0_speed: %d: %s times", ARRAY_SIZE, metricPrefix(donePerSec)));
 	}
 
 	public Object[] test_GetReleasePrimitiveArrayCritical_speed_data() {
 		return getTestArraySize();
 	}
 
 	public void test_GetReleasePrimitiveArrayCritical_speed(Object arg) {
 		final int ARRAY_SIZE = (Integer) arg;
 		final byte[] ary = new byte[ARRAY_SIZE];
 		long now = System.currentTimeMillis();
 		long startTime = now;
 		long endTime = startTime + TIME_LIMIT;
 		int done = 0;
 		while (now < endTime) {
 			Jni.loopGetReleasePrimitiveArrayCritical(ary, LOOP_PER_TICK);
 			done += LOOP_PER_TICK;
 			now = System.currentTimeMillis();
 		}
 		long timeDone = now - startTime;
 		float donePerSec = (((float) done) / timeDone) * 1000;
 		msg(String.format("test_GetReleasePrimitiveArrayCritical_speed: %d: %s times", ARRAY_SIZE, metricPrefix(donePerSec)));
 	}
 
 }
