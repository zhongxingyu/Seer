 package uk.co.mindbadger.footballresultsanalyser.domain;
 
 public interface DomainObjectFactory {
     public Season createSeason (Integer seasonNumber);
     public Division createDivision (Integer divisionId, String divisionName);
     public Team createTeam (Integer teamId, String teamName);
     public Fixture createFixture (Integer fixtureId, Season season, Team homeTeam, Team awayTeam);
     public SeasonDivision createSeasonDivision (Season season, Division division);
     public SeasonDivisionTeam createSeasonDivisionTeam (SeasonDivision seasonDivision, Team team);
     
     public SeasonDivisionId createSeasonDivisionId (Season season, Division division);
    public SeasonDivisionTeamId createSeasonDivisionTeamId (SeasonDivision seasonDivision, Team team);
 }
