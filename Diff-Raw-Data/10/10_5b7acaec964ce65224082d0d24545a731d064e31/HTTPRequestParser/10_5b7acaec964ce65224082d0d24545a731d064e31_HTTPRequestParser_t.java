 package prj.httpparser.httpparser;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import prj.httpparser.turnstile.InitializationException;
 import prj.httpparser.turnstile.StateChangeListener;
 import prj.httpparser.turnstile.StateMachine;
 import prj.httpparser.utils.EventSource;
 import prj.httpparser.wordparser.Word;
 import prj.httpparser.wordparser.WordListener;
 import prj.httpparser.wordparser.WordParser;
 
 public class HTTPRequestParser extends EventSource<HTTPParserListener> implements WordListener
 {
     private WordParser _wordParser;
     private StateMachine<HTTPRequestState, Word.WordType> _stateMachine;
     private Logger _logger;
     private StateChangeListener<HTTPRequestState, Word.WordType> _stateChangeListener = new StateChangeListener<HTTPRequestState, Word.WordType>()
     {
         @Override
         public void onChange(HTTPRequestState oldState, Word.WordType cause, HTTPRequestState newState)
         {
             try
             {
                 if (newState.equals(HTTPRequestState.METHOD_NAME_PARSED))
                 {
                     _rawHTTPRequest.setRequestType(RequestType.valueOf(_lastWord.toString().toUpperCase()));
                     resetStringBuilder();
                 }
                 else if (newState.equals(HTTPRequestState.RESOURCE_LOCATION_PARSED))
                 {
                     String[] resourceArray = _lastWord.toString().split("\\?");
                     _rawHTTPRequest.setResourceAddress(resourceArray[0]);
                     if (resourceArray.length == 2)
                     {
                         _rawHTTPRequest.setGETParams(resourceArray[1]);
                     }
                     resetStringBuilder();
                 }
                 else if (newState.equals(HTTPRequestState.HTTP_VERSION_NAME_PARSED))
                 {
                     _rawHTTPRequest.setHttpVersion(_lastWord.toString());
                     resetStringBuilder();
                 }
                 else if (newState.equals(HTTPRequestState.HEADER_FIELD_VALUE_PARSED))
                 {
                     String header = _lastWord.toString();
                    String[] headerSplit = header.split(":");
                    if (headerSplit.length == 2)
                    {
                        String field = headerSplit[0];
                        String value = headerSplit[1];
                        _rawHTTPRequest.addHeader(field, value);
                    }
                     resetStringBuilder();
                 }
                 else if (newState.equals(HTTPRequestState.FINAL))
                 {
                     fireHttpRequestArrived();
                 }
             }
             catch (Exception e)
             {
                 _logger.warn("Exception while parsing Request ", e);
                 onError();
             }
         }
     };
     private RawHTTPRequest _rawHTTPRequest;
     private StringBuilder _lastWord;
 
     public HTTPRequestParser(WordParser wordParser)
     {
         _logger = LoggerFactory.getLogger(HTTPRequestParser.class.getSimpleName());
         _wordParser = wordParser;
         _wordParser.addListener(this);
         _rawHTTPRequest = new RawHTTPRequest();
         _lastWord = new StringBuilder();
         _stateMachine = new StateMachine<>(HTTPRequestState.ERROR);
         _stateMachine.start(HTTPRequestState.START);
         initializeStateMachine();
         resetStringBuilder();
     }
 
     public void parse(String input)
     {
         _wordParser.parse(input);
     }
 
     @Override
     public void onWordArrived(Word word)
     {
         try
         {
             if (word.getType().equals(Word.WordType.WORD))
             {
                 _lastWord.append(word.getValue());
             }
             _stateMachine.process(word.getType());
         }
         catch (InitializationException | IllegalStateException e)
         {
             _logger.error("Exception in turnstile ", e);
             onError();
         }
     }
 
     @Override
     public void onError()
     {
         resetStringBuilder();
         _wordParser.reset();
         fireHttpRequestError();
     }
 
     private void resetStringBuilder()
     {
         _lastWord = new StringBuilder();
     }
 
     private void fireHttpRequestArrived()
     {
         _stateMachine.start(HTTPRequestState.START);
         for (HTTPParserListener l : _listeners)
         {
             l.onHttpRequest(_rawHTTPRequest);
         }
     }
 
     private void fireHttpRequestError()
     {
         for (HTTPParserListener l : _listeners)
         {
             l.onHttpRequestError();
         }
     }
 
     private void initializeStateMachine()
     {
         _stateMachine.start(HTTPRequestState.START);
 
         _stateMachine.addTransition(HTTPRequestState.START, Word.WordType.CRLF);
         _stateMachine.addTransition(HTTPRequestState.START, Word.WordType.WORD, HTTPRequestState.METHOD_NAME_PARSED);
 
         _stateMachine.addTransition(HTTPRequestState.METHOD_NAME_PARSED, Word.WordType.WORD, HTTPRequestState.RESOURCE_LOCATION_PARSED);
 
         _stateMachine.addTransition(HTTPRequestState.RESOURCE_LOCATION_PARSED, Word.WordType.WORD, HTTPRequestState.HTTP_VERSION_NAME_PARSED);
 
         _stateMachine.addTransition(HTTPRequestState.HTTP_VERSION_NAME_PARSED, Word.WordType.CRLF, HTTPRequestState.REQUEST_LINE_COMPLETE);
 
         _stateMachine.addTransition(HTTPRequestState.REQUEST_LINE_COMPLETE, Word.WordType.WORD, HTTPRequestState.HEADER_FIELD_NAME_PARSED);
         _stateMachine.addTransition(HTTPRequestState.REQUEST_LINE_COMPLETE, Word.WordType.CRLF, HTTPRequestState.FINAL);
 
         _stateMachine.addTransition(HTTPRequestState.HEADER_FIELD_NAME_PARSED, Word.WordType.WORD, HTTPRequestState.HEADER_FIELD_VALUE_PARSING);
         _stateMachine.addTransition(HTTPRequestState.HEADER_FIELD_NAME_PARSED, Word.WordType.CRLF, HTTPRequestState.HEADER_FIELD_VALUE_PARSED);
 
         _stateMachine.addTransition(HTTPRequestState.HEADER_FIELD_VALUE_PARSING, Word.WordType.WORD);
         _stateMachine.addTransition(HTTPRequestState.HEADER_FIELD_VALUE_PARSING, Word.WordType.CRLF, HTTPRequestState.HEADER_FIELD_VALUE_PARSED);
 
         _stateMachine.addTransition(HTTPRequestState.HEADER_FIELD_VALUE_PARSED, Word.WordType.WORD, HTTPRequestState.HEADER_FIELD_NAME_PARSED);
         _stateMachine.addTransition(HTTPRequestState.HEADER_FIELD_VALUE_PARSED, Word.WordType.CRLF, HTTPRequestState.FINAL);
 
         _stateMachine.addStateChangeListener(_stateChangeListener);
     }
 }
