 /*
  *  This file is part of Pac Defence.
  *
  *  Pac Defence is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Pac Defence is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Pac Defence.  If not, see <http://www.gnu.org/licenses/>.
  *  
  *  (C) Liam Byrne, 2008.
  */
 
 package gui;
 
 
 public class Formulae {
    
    private static final double moneyDivisor = 10;
    
    public static int numSprites(int level) {
       return 20 + 5 * level + level * level / 10;
    }
    
    public static int hp(int level) {
       return level * level + 10 * level;
    }
    
    public static int levelEndBonus(int level) {
       return 1000 + 100 * level;
    }
    
    public static int noEnemiesThroughBonus(int level) {
       return (int)(100 * Math.pow(level, 1.2));
    }
    
   public static int upgradeCost(int upgradeLevel) {
      return (int)(100 * Math.pow(upgradeLevel, 1.2));
    }
    
    public static double damageDollars(double hpLost, double hpFactor) {
       return hpFactor * hpLost / moneyDivisor;
    }
    
    public static double killBonus(int levelHP) {
       return levelHP / moneyDivisor;
    }
 
 }
