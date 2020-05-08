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
    
   private static final double moneyDivisor = 25;
    
    public static int numSprites(int level) {
       return 20 + 2 * (level - 1);
    }
    
    public static int hp(int level) {
       return (int)(Math.pow(1.4, level - 1) * 10);
    }
    
    public static int levelEndBonus(int level) {
       return 1000 + 100 * (level - 1);
    }
    
    public static int noEnemiesThroughBonus(int level) {
       return level * 100;
    }
    
    public static int upgradeCost(int currentLevel) {
       return (int)(100 * Math.pow(1.5, currentLevel - 1));
    }
    
    public static double damageDollars(double hpLost, double hpFactor) {
       return hpFactor * hpLost / moneyDivisor;
    }
    
    public static double killBonus(int levelHP) {
       return levelHP / moneyDivisor;
    }
    
    public static int nextUpgradeKills(int currentLevel) {
       return currentLevel * currentLevel * 10;
    }
    
    public static int nextUpgradeDamage(int currentLevel) {
       return (int)(Math.pow(2, currentLevel - 1)) * 200;
    }
    
    public static int towerCost(int numTowers) {
       return (int)(Math.pow(1.1, numTowers) * 1000);
    }
 
 }
