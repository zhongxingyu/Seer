 /* PokemonMoves.java
  *
  * Created April 7, 2009
  *
  * This file is a part of Shoddy Battle.
  * Copyright (C) 2009  Catherine Fitzpatrick and Benjamin Gwin
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, visit the Free Software Foundation, Inc.
  * online at http://gnu.org.
  */
 
 package shoddybattleclient.shoddybattle;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author ben
  */
 public class Pokemon implements Cloneable {
 
     public static enum Gender {
         GENDER_MALE   ("Male", 1),
         GENDER_FEMALE ("Female", 2),
         GENDER_NONE   ("None", 0),
         GENDER_BOTH   ("Both", 3);
 
         private String m_name;
         private int m_value;
         Gender(String name, int value) {
             m_name = name;
             m_value = value;
         }
         public String getName() {
             return m_name;
         }
         public int getValue() {
             return m_value;
         }
         public static Gender getGender(int value) {
             for (Gender g : Gender.values()) {
                 if (g.getValue() == value) return g;
             }
             return null;
         }
         @Override
         public String toString() {
             return m_name;
         }
     }
 
     public static final int S_HP = 0;
     public static final int S_ATTACK = 1;
     public static final int S_DEFENCE = 2;
     public static final int S_SPEED = 3;
     public static final int S_SPATTACK = 4;
     public static final int S_SPDEFENCE = 5;
     public static final int S_ACCURACY = 6;
     public static final int S_EVASION = 7;
 
     public static final int MOVE_COUNT = 4;
     public static final int STAT_COUNT = 6;
 
     public String species;
     public String nickname;
     public boolean shiny;
     public Gender gender;
     public int level;
     public String item;
     public String ability;
     public String nature;
     public String[] moves = new String[MOVE_COUNT];
     public int[] ppUps = new int[MOVE_COUNT];
     public int[] ivs = new int[STAT_COUNT];
     public int[] evs = new int[STAT_COUNT];
     public int happiness = 255;
 
     public Pokemon(String species, String nickname, boolean shiny, Gender gender,
             int level, int happiness, String item, String ability, String nature,
             String[] moves, int[] ppUps, int[] ivs, int[] evs) {
 
         this.species = species;
         this.nickname = nickname;
         this.shiny = shiny;
         this.gender = gender;
         this.level = level;
         this.happiness = happiness;
         this.item = item;
         this.ability = ability;
         this.nature = nature;
         this.moves = moves;
         this.ppUps = ppUps;
         this.ivs = ivs;
         this.evs = evs;
     }
 
     public Pokemon() {
 
     }
 
     @Override
     public String toString() {
         return this.species;
     }
 
     @Override
     public Pokemon clone() {
         return new Pokemon(species, nickname, shiny, gender, level, happiness, item,
                 ability, nature, moves.clone(), ppUps.clone(), ivs.clone(), evs.clone());
     }
 
     public int calculateStat(int i, List<PokemonSpecies> list, int level) {
         PokemonSpecies s =
                 list.get(PokemonSpecies.getIdFromName(list, species));
         PokemonNature n = PokemonNature.getNature(nature);
         return calculateStat(this, i, s, n, level);
     }
 
     private static double[] MULTIPLIERS = new double[] {0.25, 0.2857, 0.3333, 0.4, 0.5,
         0.6667, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0};
 
     public static int calculateStat(Pokemon pokemon, int i,
             PokemonSpecies species, PokemonNature nature) {
        return calculateStat(pokemon, i, species, nature, 1);
     }
 
     public static int calculateStat(Pokemon pokemon, int i,
             PokemonSpecies species, PokemonNature nature, int level)  {
         int base = species.getBase(i);
         int common =
                (int)((int)(((2.0 * base)
                 + pokemon.ivs[i]
                + (pokemon.evs[i] / 4.0)))
                * (pokemon.level / 100.0));
         if (i == Pokemon.S_HP) {
             if (base == 1) {
                 // Shedinja always has 1 hp.
                 return 1;
             } else {
                 return common + 10 + pokemon.level;
             }
         }
         double effect = (nature == null) ? 1.0 : nature.getEffect(i);
         int stat = (int)((common + 5) * effect);
         return (i == S_HP) ? stat : (int)(stat * MULTIPLIERS[6 + level]);
     }
 
     public static String getStatName(int idx) {
         switch(idx) {
             case S_HP:
                 return "HP";
             case S_ATTACK:
                 return "Atk";
             case S_DEFENCE:
                 return "Def";
             case S_SPEED:
                 return "Spd";
             case S_SPDEFENCE:
                 return "SpDef";
             case S_SPATTACK:
                 return "SpAtk";
             case S_EVASION:
                 return "Evas";
             case S_ACCURACY:
                 return "Acc";
             default:
                 return "Bad stat index";
         }
     }
 
     public String toXML() {
         StringBuffer buf = new StringBuffer();
         buf.append("<pokemon species=\"");
         buf.append(species);
         buf.append("\">\n");
         buf.append("<nickname>");
         buf.append(nickname);
         buf.append("</nickname>\n");
         if (shiny) {
             buf.append("<shiny />\n");
         }
         buf.append("<level>");
         buf.append(level);
         buf.append("</level>\n");
         buf.append("<happiness>");
         buf.append(happiness);
         buf.append("</happiness>\n");
         buf.append("<gender>");
         buf.append(gender.getName());
         buf.append("</gender>\n");
         buf.append("<nature>");
         buf.append(nature);
         buf.append("</nature>\n");
         buf.append("<item>");
         buf.append(item);
         buf.append("</item>\n");
         buf.append("<ability>");
         buf.append(ability);
         buf.append("</ability>\n");
         buf.append(("<moveset>\n"));
         for (int i = 0; i < moves.length; i++) {
             if (moves[i] == null) continue;
             buf.append(("\t<move pp-up=\""));
             buf.append(ppUps[i]);
             buf.append("\">");
             buf.append(moves[i]);
             buf.append("</move>\n");
         }
         buf.append("</moveset>\n");
         buf.append("<stats>\n");
         for (int i = 0; i < STAT_COUNT; i++) {
             buf.append("\t<stat name=\"");
             buf.append(getStatName(i));
             buf.append("\" iv=\"");
             buf.append(ivs[i]);
             buf.append("\" ev=\"");
             buf.append(evs[i]);
             buf.append("\" />\n");
         }
         buf.append("</stats>\n");
         buf.append("</pokemon>\n");
         return new String(buf);
     }
 
     public String toTeamText() {
         StringBuffer buf = new StringBuffer();
         String nick = nickname.trim();
         if(!nick.equals(species) && nick.length() != 0) {
             buf.append(nick);
             buf.append(" (");
             buf.append(species);
             buf.append(")");
         }
         else
             buf.append(species);
 
         if(gender != Gender.GENDER_NONE) {
             buf.append(" (");
             if(gender == Gender.GENDER_MALE)
                 buf.append("M");
             else
                 buf.append("F");
             buf.append(")");
         }
 
         buf.append(" @ ");
         buf.append(item);
         buf.append("\nAbility: ");
         buf.append(ability);
 
         buf.append("\nEVs: ");
         for(int i = 0; i < evs.length; i++) {
             if(evs[i] != 0) {
                 buf.append(evs[i]);
                 buf.append(" ");
                 buf.append(Pokemon.getStatName(i));
                 buf.append("/");
             }
         }
         buf.setLength(buf.length()-1); //Remove the last /
 
         PokemonNature n = PokemonNature.getNature(nature);
         buf.append("\n");
         buf.append(nature);
         buf.append(" nature (");
         if (n.getBenefits() < 0) {
             buf.append("Neutral");
         } else {
             buf.append("+");
             buf.append(Pokemon.getStatName(n.getBenefits()));
             buf.append(",-");
             buf.append(Pokemon.getStatName(n.getHarms()));
         }
         buf.append(")");
 
         for(String move : moves) {
             buf.append("\n- ");
             buf.append(move);
         }
 
         return buf.toString();
     }
 }
