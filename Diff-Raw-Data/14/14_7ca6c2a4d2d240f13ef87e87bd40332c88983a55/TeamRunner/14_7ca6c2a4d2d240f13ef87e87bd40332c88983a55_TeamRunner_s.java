 package com.powderach.fantasyteam.runner;
 
 import com.mongodb.DBCollection;
 import com.powderach.fantasyteam.*;
 import com.powderach.fantasyteam.renderer.TeamRenderer;
 import com.powderach.fantasyteam.store.PlayerSelector;
 import com.powderach.fantasyteam.store.PlayerStore;
 import com.powderach.fantasyteam.store.TeamStore;
 
 import static com.powderach.fantasyteam.store.MongoClientConnector.collectionFor;
 
 public class TeamRunner {
     public static void main(String[] args) {
        Team team = manuallyStoreTeam();
//        Team team = createTeam(teamRunner);
 
         String rendered = new TeamRenderer(team, new PlayerCostCalculator()).renderToString();
         System.out.println(rendered);
     }
 
     private static Team manuallyStoreTeam() {
         PlayerStore playerStore = new PlayerStore(collectionFor("playerdb", "player"), new JsonReader(), new JsonToPlayerFactory());
 //        getLatestData(playerStore);
 
         TeamBuilder teamBuilder = new TeamBuilder();
         teamBuilder.with(playerStore.findPlayerBy(new PlayerName("Simon", "Mignolet")));
         teamBuilder.with(playerStore.findPlayerBy(new PlayerName("Kelvin", "Davis")));
         teamBuilder.with(playerStore.findPlayerBy(new PlayerName("Per", "Mertesacker")));
         teamBuilder.with(playerStore.findPlayerBy(new PlayerName("Javier", "Garrido")));
         teamBuilder.with(playerStore.findPlayerBy(new PlayerName("Ashley", "Williams")));
         teamBuilder.with(playerStore.findPlayerBy(new PlayerName("Nathan", "Baker")));
         teamBuilder.with(playerStore.findPlayerBy(new PlayerName("Branislav", "Ivanovic")));
         teamBuilder.with(playerStore.findPlayerBy(new PlayerName("Jack", "Colback")));
         teamBuilder.with(playerStore.findPlayerBy(new PlayerName("Eden", "Hazard")));
         teamBuilder.with(playerStore.findPlayerBy(new PlayerName("Gnegneri Yaya", "Touré")));
         teamBuilder.with(playerStore.findPlayerBy(new PlayerName("Emboaba", "Oscar")));
         teamBuilder.with(playerStore.findPlayerBy(new PlayerName("Christian", "Benteke")));
         teamBuilder.with(playerStore.findPlayerBy(new PlayerName("Robin", "van Persie")));
         teamBuilder.with(playerStore.findPlayerBy(new PlayerName("Luke", "Moore")));
         Team team = teamBuilder.build();
 
         TeamStore teamStore = new TeamStore(collectionFor("playerdb", "team"));
         teamStore.store(team);
         return team;
     }
 
     private static void getLatestData(PlayerStore playerStore) {
         playerStore.getDataUpToPlayerNumber(532);
     }
 
    private static Team createTeam() {
        DBCollection playerCollection = collectionFor("playerdb", "player");
         PlayerSelector playerSelector = new PlayerSelector(playerCollection);
        TeamSelector teamSelector = new TeamSelector(playerSelector, null);
         return teamSelector.selectAndStore();
     }
 
 }
