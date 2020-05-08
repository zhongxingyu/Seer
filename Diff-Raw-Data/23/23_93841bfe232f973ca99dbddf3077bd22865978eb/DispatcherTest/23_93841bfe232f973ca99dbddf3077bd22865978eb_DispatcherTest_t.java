 package org.deschutter;
 
 import org.deschutter.analyzer.FightAnalyzer;
 import org.deschutter.parser.DamageDoneParser;

import java.io.BufferedReader;
 import java.io.FileReader;
 import org.deschutter.parser.exception.FileNullException;
 import org.deschutter.parser.ParsedLine;
 import org.junit.Test;

import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.List;
 import org.deschutter.parser.IParser;
 import org.junit.Before;
 import org.springframework.stereotype.Component;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.Matchers.any;
 
 /**
  *
  * @author berten
  */
 
 public class DispatcherTest {
     private FightAnalyzer analyzer;
     private List<IParser> parsers;
     IParser attackParser;
     Dispatcher dispatcher;
 
     @Before
     public void setUp() {
         attackParser = mock(DamageDoneParser.class);
         parsers = Arrays.asList(attackParser);
         analyzer = mock(FightAnalyzer.class);
         dispatcher = new Dispatcher(parsers, analyzer);
     }
     
     @Test
     public void analyse_callsParser_canHandle() throws Exception {
        dispatcher.dispatch(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("oneLineParse.txt")));
         verify(attackParser).canHandle(any(ParsedLine.class));
     }
         
     @Test
     public void analyse_parserCanHandle_Must_handle() throws Exception {
         when(attackParser.canHandle(any(ParsedLine.class))).thenReturn(Boolean.TRUE);
        dispatcher.dispatch(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("oneLineParse.txt")));
         verify(attackParser).handle(any(ParsedLine.class));
     }
     @Test
     public void dispatch_CallsAnalyzer() throws Exception {
         dispatcher.dispatch(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("oneLineParse.txt")));
          verify(analyzer).analyzeFight(any(Fight.class));
     }
     
     @Test(expected = FileNullException.class)
     public void nullFile_givesError() {
         dispatcher.dispatch(null);
     }
 }
