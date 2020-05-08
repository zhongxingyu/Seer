 package no.anderska.wta.servlet;
 
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import no.anderska.wta.AnswerStatus;
 import no.anderska.wta.GameHandler;
 import no.anderska.wta.QuestionList;
 import no.anderska.wta.dto.PlayerAnswerDto;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.ArgumentMatcher;
 import org.mockito.Matchers;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.fest.assertions.Assertions.*;
 import static org.mockito.Mockito.*;
 
 public class GameServletTest {
 
     private final GameServlet servlet = new GameServlet();
     private final HttpServletRequest req = mock(HttpServletRequest.class);
     private final HttpServletResponse resp = mock(HttpServletResponse.class);
     private final StringWriter htmlSource = new StringWriter();
     private final GameHandler gameHandler = mock(GameHandler.class);
 
     @Test
     public void shouldHandleAnswer() throws Exception {
         when(req.getMethod()).thenReturn("POST");
 
         PlayerAnswerDto playerAnswerDto = new PlayerAnswerDto();
         playerAnswerDto.setPlayerId("playerone");
         playerAnswerDto.setAnswers(Arrays.asList("one", "two", "three"));
         Gson gson = new Gson();
         when(req.getReader()).thenReturn(new BufferedReader(new StringReader(gson.toJson(playerAnswerDto))));
 
 
         when(gameHandler.answer(anyString(), anyList())).thenReturn(AnswerStatus.OK);
 
         servlet.service(req, resp);
 
         Class<List<String>> listClass = (Class<List<String>>)(Class)List.class;
         ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(listClass);
 
         verify(gameHandler).answer(Matchers.eq("playerone"), captor.capture());
 
         List<String> givenAnswers = captor.getValue();
         assertThat(givenAnswers).hasSize(3).contains("one", "two", "three");
         assertThat(htmlSource.toString()).isEqualTo("{\"status\" : \"OK\"}");
     }
 
     @Test
     public void shouldHandleIllegalJson() throws Exception {
         when(req.getMethod()).thenReturn("POST");
         when(req.getReader()).thenReturn(new BufferedReader(new StringReader("xxx")));
 
         servlet.service(req, resp);
 
         assertThat(htmlSource.toString()).isEqualTo("{\"status\" : \"ERROR\"}");
         verify(gameHandler,never()).answer(anyString(),anyList());
     }
 
     @Test
     public void shouldGiveQuestions() throws Exception {
         when(req.getMethod()).thenReturn("GET");
         when(req.getParameter("playerid")).thenReturn("playerone");
         when(req.getParameter("category")).thenReturn("catid");
 
         when(gameHandler.questions(anyString(),anyString())).thenReturn(new QuestionList(Arrays.asList("q1", "q2", "q3")));
 
 
         servlet.service(req, resp);
 
         verify(gameHandler).questions("playerone","catid");
         verify(resp).setStatus(HttpServletResponse.SC_OK);
         verify(resp).setContentType("text/json");
 
         Gson gson = new Gson();
 
         List<String> answers = gson.fromJson(htmlSource.toString(), new TypeToken<List<String>>() {}.getType());
 
         assertThat(answers).containsExactly("q1","q2","q3");
     }
 
     @Test
     public void shouldGiveErrorOnMissingParameters() throws Exception {
         when(req.getMethod()).thenReturn("GET");
         when(req.getParameter("playerid")).thenReturn("playerone");
 
         servlet.service(req, resp);
 
         verify(gameHandler,never()).questions(anyString(),anyString());
         verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST,"Missing parameter category");
     }
 
     @Before
     public void setup() throws IOException {
         when(resp.getWriter()).thenReturn(new PrintWriter(htmlSource));
         servlet.setGameHandler(gameHandler);
     }
 }
