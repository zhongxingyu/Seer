 package no.anderska.wta.servlet;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import no.anderska.wta.StatusGiver;
 import no.anderska.wta.dto.CategoryDTO;
 import no.anderska.wta.dto.GameStatusDTO;
 import no.anderska.wta.dto.PlayerDTO;
 
 import no.anderska.wta.game.AdminHandler;
 import org.dom4j.DocumentHelper;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.gson.Gson;
 
 public class StatusServletTest {
 
     private final StatusServlet servlet = new StatusServlet();
     private final HttpServletRequest req = mock(HttpServletRequest.class);
     private final HttpServletResponse resp = mock(HttpServletResponse.class);
     private final StringWriter htmlSource = new StringWriter();
     private final AdminHandler adminHandler = mock(AdminHandler.class);
 
     @Test
     public void shouldGiveListOfCategories() throws Exception {
         when(req.getMethod()).thenReturn("GET");
 
         StatusGiver statusGiver = mock(StatusGiver.class);
         when(statusGiver.catergoryStatus()).thenReturn(Arrays.asList(new CategoryDTO("1","one",1,null),new CategoryDTO("2","two",2,"Someone")));
         servlet.setStatusGiver(statusGiver);
 
         PlayerHandler playerHandler = mock(PlayerHandler.class);
         when(playerHandler.playerList()).thenReturn(Arrays.asList(new PlayerDTO("Player one",10),new PlayerDTO("Player two",15)));
         servlet.setPlayerHandler(playerHandler);
 
         servlet.service(req, resp);
 
         Gson gson = new Gson();
         GameStatusDTO gameStatusDTO = gson.fromJson(htmlSource.toString(), GameStatusDTO.class);
 
         List<CategoryDTO> categories = gameStatusDTO.getCategories();
 
         assertThat(categories).hasSize(2);
 
         assertThat(categories.get(0).getId()).isEqualTo("1");
         assertThat(categories.get(0).getAnsweredBy()).isNull();
         assertThat(categories.get(1).getId()).isEqualTo("2");
         assertThat(categories.get(1).getAnsweredBy()).isEqualTo("Someone");
 
         List<PlayerDTO> players = gameStatusDTO.getPlayers();
 
         assertThat(players).hasSize(2);
     }
 
     @Test
     public void shouldRestartGame() throws Exception {
         when(req.getMethod()).thenReturn("POST");
         when(req.getParameter("password")).thenReturn("secret");
        when(req.getParameter("action")).thenReturn("resetAll");
 
         servlet.service(req, resp);
 
         verify(resp).setContentType("text/html");
         verify(adminHandler).restartGame("secret");
        assertThat(htmlSource.toString()).contains("Action performed");
 
         DocumentHelper.parseText(htmlSource.toString());
 
     }
 
 
     @Test
     public void sholdGiveIndicationOnWrongPassword() throws Exception {
         when(req.getMethod()).thenReturn("POST");
        when(req.getParameter("action")).thenReturn("resetAll");
 
         when(adminHandler.restartGame(anyString())).thenReturn("Wrong password");
 
         servlet.service(req, resp);
 
         assertThat(htmlSource.toString()).contains("Wrong password");
 
         DocumentHelper.parseText(htmlSource.toString());
 
     }
 
     @Before
     public void setup() throws IOException {
         when(resp.getWriter()).thenReturn(new PrintWriter(htmlSource));
         servlet.setAdminHandler(adminHandler);
     }
 
 
 }
