 package org.fam.ejb.model;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import lombok.Getter;
 import lombok.Setter;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * @author gbougear
  */
 @Getter
 @Setter
 @Entity
 @Table(name = FamMatchTeam.TABLE_NAME)
 @IdClass(FamMatchTeamPK.class)
 @XmlRootElement
 public class FamMatchTeam implements Serializable {
 
     private static final long serialVersionUID = 4746992926937676582L;
     /**
      *
      */
     public static final String TABLE_NAME = "fam_match_team";
     //
     /**
      *
      */
     public static final String COL_ID_MATCH = "id_match";
     /**
      *
      */
     public static final String PROP_MATCH = "famMatch";
     @Id
     @ManyToOne(cascade = CascadeType.ALL)
     @JoinColumn(name = COL_ID_MATCH, referencedColumnName = FamMatch.COL_ID)
     @NotNull
     private FamMatch famMatch;
     //
     /**
      *
      */
     public static final String COL_ID_TEAM = "id_team";
     /**
      *
      */
     public static final String PROP_TEAM = "famTeam";
     @Id
     @ManyToOne(cascade = CascadeType.ALL)
     @JoinColumn(name = COL_ID_TEAM, referencedColumnName = FamTeam.COL_ID)
     @NotNull
     private FamTeam famTeam;
     //
     /**
      *
      */
     public static final String PROP_HOME = "home";
     @Column(name = PROP_HOME)
     private Boolean home;
     //
     /**
      *
      */
     public static final String PROP_POINTS = "points";
     @Basic(optional = false)
     @Column(name = PROP_POINTS)
     private Integer points;
     //
     /**
      *
      */
     public static final String PROP_VICTORY = "victory";
     @Basic(optional = false)
     @Column(name = PROP_VICTORY)
     private Integer victory;
     //
     /**
      *
      */
     public static final String PROP_DRAW = "draw";
     @Basic(optional = false)
     @Column(name = PROP_DRAW)
     private Integer draw;
     //
     /**
      *
      */
     public static final String PROP_DEFEAT = "defeat";
     @Basic(optional = false)
     @Column(name = PROP_DEFEAT)
     private Integer defeat;
     //
     /**
      *
      */
     public static final String PROP_GOAL_SCORED = "goal_scored";
     @Column(name = PROP_GOAL_SCORED)
     private Integer goalScored;
     //
     /**
      *
      */
     public static final String PROP_GOAL_SHIPPED = "goal_shipped";
     @Column(name = PROP_GOAL_SHIPPED)
     private Integer goalShipped;
     //
     @OneToMany(mappedBy = FamMatchPlayer.PROP_MATCH_TEAM)
     @OrderBy(FamMatchPlayer.PROP_NUM)
     private List<FamMatchPlayer> famMatchPlayerList;
     //
     /**
      *
      */
     public static final String COL_ID_FORMATION = "id_formation";
     /**
      *
      */
     public static final String PROP_FORMATION = "famFormation";
     @ManyToOne(cascade = CascadeType.ALL)
     @JoinColumn(name = COL_ID_FORMATION, referencedColumnName = FamFormation.COL_ID)
     private FamFormation famFormation;
     //
     /**
      *
      */
     public static final String PROP_DRAFT = "draft";
     @Column(name = PROP_DRAFT)
     private Boolean draft;
 
     /**
      *
      */
     public FamMatchTeam() {
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
         if (o == null || getClass() != o.getClass()) {
             return false;
         }
 
         FamMatchTeam that = (FamMatchTeam) o;
 
         return famMatch.equals(that.famMatch) && famTeam.equals(that.famTeam);
 
     }
 
     @Override
     public int hashCode() {
         int result = famMatch.hashCode();
         result = 31 * result + famTeam.hashCode();
         return result;
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append("FamMatchTeam");
         sb.append("{famMatch=").append(famMatch.getFamEvent().getLibEvent());
         sb.append(", famTeam=").append(famTeam.getLibTeam());
         sb.append(", home=").append(home);
         sb.append(", points=").append(points);
         sb.append(", victory=").append(victory);
         sb.append(", draw=").append(draw);
         sb.append(", defeat=").append(defeat);
         sb.append(", goalScored=").append(goalScored);
         sb.append(", goalShipped=").append(goalShipped);
 //        sb.append(", famMatchPlayerList=").append(famMatchPlayerList);
        sb.append(", famFormation=").append(famFormation.getLibFormation());
         sb.append(", draft=").append(draft);
         sb.append('}');
         return sb.toString();
     }
 
     //    @PrePersist
 //    private void addEvent() {
 ////
 //
 //        // Event
 //        FamEvent event = null;
 //        if (famMatch.getFamEvent() == null) {
 //            event = new FamEvent();
 //        } else {
 //            event = famMatch.getFamEvent();
 //        }
 //
 //        event.setFamSeason(getFamMatch().getFamSeasonCompetition().getFamSeason());
 //        event.setDuration(90);
 //
 //        FamTeam team = getFamTeam();
 //        if (getHome() == true) {
 //            event.setFamPlace(team.getFamPlace());
 //        }
 //
 //        if (event.getFamTeamList() == null) {
 //            event.setFamTeamList(new ArrayList<FamTeam>());
 //        }
 //        event.getFamTeamList().add(team);
 //
 //        String lib = team.getLibTeam();
 //        if (event.getFamTeamList().size() == 2) {
 //            if (getHome() == true) {
 //                lib = team.getLibTeam() + " - " + event.getLibEvent();
 //            } else {
 //                lib = event.getLibEvent() + " - " + team.getLibTeam();
 //            }
 //        }
 //        event.setLibEvent(lib);
 //
 //
 //        famMatch.setFamEvent(event);
 //    }
 }
