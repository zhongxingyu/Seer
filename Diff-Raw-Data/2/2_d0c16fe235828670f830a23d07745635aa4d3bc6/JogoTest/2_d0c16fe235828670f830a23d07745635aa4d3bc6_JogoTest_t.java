 package com.fear_airsoft.json;
 
 import junit.framework.TestCase;
 import com.google.gson.Gson;
 
 public class JogoTest extends TestCase {
     Gson gson;
     static final String jsonFilled = "[{\"id\":\"oui1djr17p9b9jmopsblenrfq8\",\"horaInicio\":9,\"campo\":{\"id\":\"BMC\",\"morada\":\"Rua da Grotinha, Arrifes, Ilha de São Miguel\",\"estacionamentoLng\":-25.681646,\"lng\":-25.681646,\"nome\":\"Baterias do monte da Castanheira\",\"foto\":[],\"lat\":37.755184,\"estacionamentoLat\":37.755184},\"mes\":3,\"horaFim\":13,\"minutosInicio\":0,\"ano\":2013,\"dia\":2,\"nome\":\"Op. Golden Bridge 2\",\"minutosFim\":0,\"descricao\":\"teste\"}]";
     static final String jsonFilledWithFotos = "[{\"id\":\"oui1djr17p9b9jmopsblenrfq8\",\"horaInicio\":9,\"campo\":{\"id\":\"BMC\",\"morada\":\"Rua da Grotinha, Arrifes, Ilha de São Miguel\",\"estacionamentoLng\":-25.681646,\"lng\":-25.681646,\"nome\":\"Baterias do monte da Castanheira\",\"foto\":[\"1.jpg\",\"2.jpg\"],\"lat\":37.755184,\"estacionamentoLat\":37.755184},\"mes\":3,\"horaFim\":13,\"minutosInicio\":0,\"ano\":2013,\"dia\":2,\"nome\":\"Op. Golden Bridge 2\",\"minutosFim\":0,\"descricao\":\"teste\"}]";
     static final String jsonEmpty = "[]";
  
     public void setUp() {
         gson = new Gson();
     }
 
     public void tearDown() {
         gson = null;
     }
 
     public void testJsonFilled() {
         Jogo[] jogos = gson.fromJson(jsonFilled, Jogo[].class);
         assertEquals(jogos.length, 1);
         Jogo jogo = jogos[0];
         assertEquals(jogo.getId(), "oui1djr17p9b9jmopsblenrfq8");
         assertEquals(jogo.getNome(), "Op. Golden Bridge 2");
         assertEquals(jogo.getAno(), 2013);
         assertEquals(jogo.getMes(), 3);
         assertEquals(jogo.getDia(), 2);
         assertEquals(jogo.getHoraInicio(), 9);
         assertEquals(jogo.getMinutosInicio(), 0);
         assertEquals(jogo.getHoraFim(), 13);
         assertEquals(jogo.getMinutosFim(), 0);
         assertEquals(jogo.getDescricao(), "teste");
         assertEquals(jogo.getCampo().getId(), "BMC");
         assertEquals(jogo.getCampo().getNome(), "Baterias do monte da Castanheira");
         assertEquals(jogo.getCampo().getMorada(), "Rua da Grotinha, Arrifes, Ilha de São Miguel");
         assertEquals(jogo.getCampo().getLng(), -25.681646, 0.00001);
         assertEquals(jogo.getCampo().getEstacionamentoLng(), -25.681646, 0.00001);
         assertEquals(jogo.getCampo().getLat(), 37.755184, 0.00001);
         assertEquals(jogo.getCampo().getEstacionamentoLat(), 37.755184, 0.00001);
         assertEquals(jogo.getCampo().getFoto().size(), 0);
     }
     
     public void testJsonFilledWithFotos() {
        Jogo[] jogos = gson.fromJson(jsonFilledWithFotos, Jogo[].class);
         assertEquals(jogos.length, 1);
         Jogo jogo = jogos[0];
         assertEquals(jogo.getCampo().getFoto().size(), 2);
         assertEquals(jogo.getCampo().getFoto().get(0), "1.jpg");
         assertEquals(jogo.getCampo().getFoto().get(1), "2.jpg");
     }
     
     public void testJsonEmpty() {
         Jogo[] jogos = gson.fromJson(jsonEmpty, Jogo[].class);
         assertEquals(jogos.length, 0);
     }
 }
