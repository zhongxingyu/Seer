 package org.jfugue.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 import org.jfugue.ParserListener;
 import org.jfugue.ParserProgressListener;
 import org.jfugue.elements.ChannelPressure;
 import org.jfugue.elements.Controller;
 import org.jfugue.elements.Instrument;
 import org.jfugue.elements.KeySignature;
 import org.jfugue.elements.Layer;
 import org.jfugue.elements.Measure;
 import org.jfugue.elements.Note;
 import org.jfugue.elements.PitchBend;
 import org.jfugue.elements.PolyphonicPressure;
 import org.jfugue.elements.SystemExclusive;
 import org.jfugue.elements.Tempo;
 import org.jfugue.elements.Time;
 import org.jfugue.elements.Voice;
 import org.jfugue.parsers.Parser;
 
import org.junit.*;
 
 public class ParserTest {
     public class TestParser extends Parser {
 
 	}
 
 	private Parser parser;
 
     @Before
     public void setUp() {
         parser = new TestParser();
     }
 
 
     @Test 
     public void testProgressListenerList_DefaultState(){
         assertEquals("ProgressListenerList should be empty by default", 0, parser.getParserProgressListeners().length);
     }
 
     @Test 
     public void testAddParserProgressListener_Once(){
         ParserProgressListener listener = mock(ParserProgressListener.class);
         parser.addParserProgressListener(listener);
         assertEquals("A listener could not be added to the ProgressListenerList", 1, parser.getParserProgressListeners().length);
     }
 
     @Test 
     public void testRemoveParserProgressListener_Once(){
         ParserProgressListener listener = mock(ParserProgressListener.class);
         parser.addParserProgressListener(listener);
         parser.removeParserProgressListener(listener);
         assertEquals("Adding and removing a listener did not leave the ProgressListenerList empty", 0,parser.getParserProgressListeners().length);
     }
 
     @Test 
     public void testRemoveParserProgressListener_Too_Many(){
         ParserProgressListener listener = mock(ParserProgressListener.class);
         parser.addParserProgressListener(listener);
         parser.removeParserProgressListener(listener);
         parser.removeParserProgressListener(listener);
         assertEquals("Removing a ParseProgressListener twice should have the same effect as removing it once", 0, parser.getParserProgressListeners().length);
     }
 
     @Test 
     public void testListenerList_DefaultState(){
         assertEquals("ParserListenerList should be empty by default", 0,parser.getParserListeners().length);
     }
 
     @Test 
     public void testAddParserListener_Once(){
         ParserListener listener = mock(ParserListener.class);
         parser.addParserListener(listener);
         assertEquals("A listener could not be added to the ParserListenerList", 1,parser.getParserListeners().length);
     }
 
     @Test 
     public void testRemoveParserListener_Once(){
         ParserListener listener = mock(ParserListener.class);
         parser.addParserListener(listener);
         parser.removeParserListener(listener);
         assertEquals("Adding and removing a listener did not leave the PsrserListenerList empty", 0,parser.getParserListeners().length);
     }
 
     @Test 
     public void testRemoveParserListener_MoreThanAdd(){
         ParserListener listener = mock(ParserListener.class);
         @SuppressWarnings("unused")
 		ParserListener listener0 = mock(ParserListener.class);
         parser.addParserListener(listener);
         parser.removeParserListener(listener);
         parser.removeParserListener(listener);
         assertEquals("Removing a ParserListener twice should have the same effect as removing it once", 0, parser.getParserListeners().length);
     }
 
     @Test 
     public void testRemoveParserListener_Check_Correct_Removal(){
         ParserListener listener0 = mock(ParserListener.class);
         ParserListener listener1 = mock(ParserListener.class);
         ParserListener listener2 = mock(ParserListener.class);
         parser.addParserListener(listener0);
         parser.addParserListener(listener1);
         parser.addParserListener(listener2);
         parser.removeParserListener(listener0);
         parser.removeParserListener(listener2);
         assertEquals("Removing a ParserListener twice should have the same effect as removing it once", listener1, parser.getParserListeners()[0]);
     }
 
     private ParserListener getNotifiedListener(final String methodName, final Object arg) {
 	return getNotifiedListeners(1, methodName, arg)[0];
     }
 
     private ParserListener [] getNotifiedListeners(final int listenerCount, final String methodName, final Object arg) {
         ParserListener [] mockListeners = new ParserListener[listenerCount];
 	for(int i = 0; i < listenerCount; ++i) {
 	    mockListeners[i] = mock(ParserListener.class);
 	    parser.addParserListener(mockListeners[i]);
 	}
 	callEventMethod(methodName,arg);
 	return mockListeners;
     }
 
     private void callEventMethod(final String methodName, final Object arg) {
 	@SuppressWarnings("rawtypes")
 	final Class [] argTypes = {arg.getClass()};
 	final Object [] args = {arg};
 	invokeParserMethod(methodName,argTypes,args);
     }
 
     private Object invokeParserMethod(final String methodName, @SuppressWarnings("rawtypes") final Class [] methodArgTypes,  final Object [] methodArgs) {
 	return TestCaseHelpers.invokeRestrictedMethod(parser, Parser.class, methodName, methodArgTypes, methodArgs);
     }
     
     @Test 
     public void testfireVoiceEvent_Zero() {
 	final Voice element = new Voice((byte)0);
         getNotifiedListeners(0,"fireVoiceEvent", element);
     }
 
     @Test 
     public void testfireVoiceEvent_One() {
 	final Voice element = new Voice((byte)0);
         verify(getNotifiedListener("fireVoiceEvent", element)).voiceEvent(element);	
     }
 
     @Test 
     public void testfireVoiceEvent_Many() {
 	final Voice element = new Voice((byte)0);
 	ParserListener [] listeners = getNotifiedListeners(3, "fireVoiceEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).voiceEvent(element);
 	}
     }
 
     @Test 
     public void testfireTempoEvent_Zero() {
 	final Tempo element = new Tempo(0);
         getNotifiedListeners(0,"fireTempoEvent", element);
     }
 
     @Test 
     public void testfireTempoEvent_One() {
 	final Tempo element = new Tempo(0);
         verify(getNotifiedListener("fireTempoEvent", element)).tempoEvent(element);
     }
 
     @Test 
     public void testfireTempoEvent_Many() {
 	final Tempo element = new Tempo(0);
 	ParserListener [] listeners = getNotifiedListeners(3,"fireTempoEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).tempoEvent(element);
 	}
     }
 
     @Test 
     public void testfireInstrumentEvent_Zero() {
 	final Instrument element = new Instrument((byte)0);
         getNotifiedListeners(0,"fireInstrumentEvent", element);
     }
 
     @Test 
     public void testfireInstrumentEvent_One() {
 	final Instrument element = new Instrument((byte)0);
         verify(getNotifiedListener("fireInstrumentEvent", element)).instrumentEvent(element);
     }
 
     @Test 
     public void testfireInstrumentEvent_Many() {
 	final Instrument element = new Instrument((byte)0);
 	ParserListener [] listeners = getNotifiedListeners(3, "fireInstrumentEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).instrumentEvent(element);
 	}
     }
 
     @Test 
     public void testfireLayerEvent_Zero() {
 	final Layer element = new Layer((byte)0);
         getNotifiedListeners(0,"fireLayerEvent", element);
     }
 
     @Test 
     public void testfireLayerEvent_One() {
 	final Layer element = new Layer((byte)0);
         verify(getNotifiedListener("fireLayerEvent", element)).layerEvent(element);
     }
 
     @Test 
     public void testfireLayerEvent_Many() {
 	final Layer element = new Layer((byte)0);
 	ParserListener [] listeners = getNotifiedListeners(3, "fireLayerEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).layerEvent(element);
 	}
     }
 
     @Test
     public void testfireTimeEvent_Zero() {
 	final Time element = new Time(0);
         getNotifiedListeners(0,"fireTimeEvent", element);
     }
 
     @Test
     public void testfireTimeEvent_One() {
 	final Time element = new Time(0);
         verify(getNotifiedListener("fireTimeEvent", element)).timeEvent(element);
     }
 
     @Test
     public void testfireTimeEvent_Many() {
 	final Time element = new Time(0);
 	ParserListener [] listeners = getNotifiedListeners(3, "fireTimeEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).timeEvent(element);
 	}
     }
 
     @Test 
     public void testfireSystemExclusiveEvent_Zero() {
 	final SystemExclusive element = new SystemExclusive(null);
         getNotifiedListeners(0,"fireSystemExclusiveEvent", element);
     }    
 
     @Test 
     public void testfireSystemExclusiveEvent_One() {
 	final SystemExclusive element = new SystemExclusive(null);
         verify(getNotifiedListener("fireSystemExclusiveEvent", element)).systemExclusiveEvent(element);
     }    
 
     @Test 
     public void testfireSystemExclusiveEvent_Many() {
 	final SystemExclusive element = new SystemExclusive(null);
 	ParserListener [] listeners = getNotifiedListeners(3, "fireSystemExclusiveEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).systemExclusiveEvent(element);
 	}
     }    
 
     @Test
     public void testfireKeySignatureEvent_Zero() {
 	final KeySignature element = new KeySignature((byte)0, (byte)0);
         getNotifiedListeners(0,"fireKeySignatureEvent", element);
     }
 
     @Test
     public void testfireKeySignatureEvent_One() {
 	final KeySignature element = new KeySignature((byte)0, (byte)0);
         verify(getNotifiedListener("fireKeySignatureEvent", element)).keySignatureEvent(element);
     }
 
     @Test
     public void testfireKeySignatureEvent_Many() {
 	final KeySignature element = new KeySignature((byte)0, (byte)0);
 	ParserListener [] listeners = getNotifiedListeners(3, "fireKeySignatureEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).keySignatureEvent(element);
 	}
     }
 
     @Test 
     public void testfireMeasureEvent_Zero() {
 	final Measure element = new Measure();
         getNotifiedListeners(0,"fireMeasureEvent", element);
     }
 
     @Test 
     public void testfireMeasureEvent_One() {
 	final Measure element = new Measure();
         verify(getNotifiedListener("fireMeasureEvent", element)).measureEvent(element);
     }
 
     @Test 
     public void testfireMeasureEvent_Many() {
 	final Measure element = new Measure();
 	ParserListener [] listeners = getNotifiedListeners(3, "fireMeasureEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).measureEvent(element);
 	}
     }
 
     @Test 
     public void testfireControllerEvent_Zero() {
 	final Controller element = new Controller();
         getNotifiedListeners(0,"fireControllerEvent", element);
     }
 
     @Test 
     public void testfireControllerEvent_One() {
 	final Controller element = new Controller();
         verify(getNotifiedListener("fireControllerEvent", element)).controllerEvent(element);
     }
 
     @Test 
     public void testfireControllerEvent_Many() {
 	final Controller element = new Controller();
 	ParserListener [] listeners = getNotifiedListeners(3, "fireControllerEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).controllerEvent(element);
 	}
     }
 
     @Test 
     public void testfireChannelPressureEvent_Zero() {
 	final ChannelPressure element = new ChannelPressure((byte)0);
         getNotifiedListeners(0,"fireChannelPressureEvent", element);
     }
 
     @Test 
     public void testfireChannelPressureEvent_One() {
 	final ChannelPressure element = new ChannelPressure((byte)0);
         verify(getNotifiedListener("fireChannelPressureEvent", element)).channelPressureEvent(element);
     }
 
     @Test 
     public void testfireChannelPressureEvent_Many() {
 	final ChannelPressure element = new ChannelPressure((byte)0);
 	ParserListener [] listeners = getNotifiedListeners(3, "fireChannelPressureEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).channelPressureEvent(element);
 	}
     }
 
     @Test 
     public void testfirePolyphonicPressureEvent_Zero() {
 	final PolyphonicPressure element = new PolyphonicPressure((byte)0, (byte)0);
         getNotifiedListeners(0,"firePolyphonicPressureEvent", element);
     }
     
     @Test 
     public void testfirePolyphonicPressureEvent_One() {
 	final PolyphonicPressure element = new PolyphonicPressure((byte)0, (byte)0);
         verify(getNotifiedListener("firePolyphonicPressureEvent", element)).polyphonicPressureEvent(element);
     }
     
     @Test 
     public void testfirePolyphonicPressureEvent_Many() {
 	final PolyphonicPressure element = new PolyphonicPressure((byte)0, (byte)0);
 	ParserListener [] listeners = getNotifiedListeners(3, "firePolyphonicPressureEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).polyphonicPressureEvent(element);
 	}
     }
     
     @Test 
     public void testfirePitchBendEvent_Zero() {
 	final PitchBend element = new PitchBend((byte)0, (byte)0);
         getNotifiedListeners(0,"firePitchBendEvent", element);
     }    
 
     @Test 
     public void testfirePitchBendEvent_One() {
 	final PitchBend element = new PitchBend((byte)0, (byte)0);
         verify(getNotifiedListener("firePitchBendEvent", element)).pitchBendEvent(element);
     }    
 
     @Test 
     public void testfirePitchBendEvent_Many() {
 	final PitchBend element = new PitchBend((byte)0, (byte)0);
 	ParserListener [] listeners = getNotifiedListeners(3, "firePitchBendEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).pitchBendEvent(element);
 	}
     }    
 
     @Test 
     public void testfireNoteEvent_Zero() {
 	final Note element = new Note((byte)0);
         getNotifiedListeners(0,"fireNoteEvent", element);
     }
 
     @Test 
     public void testfireNoteEvent_One() {
 	final Note element = new Note((byte)0);
         verify(getNotifiedListener("fireNoteEvent", element)).noteEvent(element);
     }
 
     @Test 
     public void testfireNoteEvent_Many() {
 	final Note element = new Note((byte)0);
 	ParserListener [] listeners = getNotifiedListeners(3, "fireNoteEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).noteEvent(element);
 	}
     }
 
     @Test 
     public void testfireSequentialNoteEvent_Zero() {
 	final Note element = new Note((byte)0);
         getNotifiedListeners(0,"fireSequentialNoteEvent", element);
     }
 
     public void testfireSequentialNoteEvent_One() {
 	final Note element = new Note((byte)0);
         verify(getNotifiedListener("fireSequentialNoteEvent", element)).sequentialNoteEvent(element);
     }
 
     @Test 
     public void testfireSequentialNoteEvent_Many() {
 	final Note element = new Note((byte)0);
 	ParserListener [] listeners = getNotifiedListeners(3, "fireSequentialNoteEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).sequentialNoteEvent(element);
 	}
     }
 
     @Test
     public void testfireParallelNoteEvent_Zero() {
 	final Note element = new Note((byte)0);
         getNotifiedListeners(0,"fireParallelNoteEvent", element);
     }
 
     @Test
     public void testfireParallelNoteEvent_One() {
 	final Note element = new Note((byte)0);
         verify(getNotifiedListener("fireParallelNoteEvent", element)).parallelNoteEvent(element);
     }
     
     @Test
     public void testfireParallelNoteEvent_Many() {
 	final Note element = new Note((byte)0);
 	ParserListener [] listeners = getNotifiedListeners(3, "fireParallelNoteEvent", element);
 	for (int i = 0; i < 3; ++i) {
 	    verify(listeners[i]).parallelNoteEvent(element);
 	}
     }
 
 }
