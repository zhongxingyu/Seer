 package com.managehelper.model.impl;
 
 import com.managehelper.model.Team;
 import com.managehelper.model.TeamRateEvaluator;
 
 public class TeamRateEvaluatorImpl implements TeamRateEvaluator {
 
     @Override
     public double evaluateIndex(int[][] array, Team team) {
         int n = team.getParticipants();
         if (n == 0) {
             throw new IllegalArgumentException("Number of participants is null");
         }
 
         int VP = 0;
         for (int i = 0; i < n - 1; i++) {
             for (int j = i + 1; j < n; j++) {
                 if ((array[i][j] == array[j][i]) && (array[i][j] == 1)) {
                     VP++;
                 }
             }
         }

        final double v = VP / (double) n * ((double) n - 1.d);
        team.setIndex(v);
        return v;
     }
 
     @Override
     public double evaluateGroupUnity(int[][] array, Team team) {
        team.setGroupUnity(0);
         return 0;
     }
 
     @Override
     public double evaluateMedianaPlus(int[][] array, Team team) {
        int resoult;
         int n = team.getParticipants();
         int massiv[] = new int[n];
 
         for (int i = 0; i < n; i++) {
             for (int j = 0; j < n; j++) {
                 if (array[i][j] == 1) {
                    massiv[i] += 1;
                 }
 
             }
         }
         sort(massiv);
         if (n % 2 == 0) {
             resoult = massiv[(massiv.length / 2) + 1];
 
         } else {
             resoult = massiv[(massiv.length + 1) / 2];
 
         }
        team.setMedianaPlus(resoult);
         return resoult;
     }
 
     @Override
     public double evaluateMedianaMinus(int[][] array, Team team) {
 
         int resoult = 0;
         int n = team.getParticipants();
         int massiv[] = new int[n];
 
         for (int i = 0; i < n; i++) {
             for (int j = 0; j < n; j++) {
                 if (array[i][j] == 0) {
                    massiv[i] += 1;
                 }
 
             }
         }
         sort(massiv);
         if (n % 2 == 0) {
             resoult = massiv[(massiv.length / 2) + 1];
 
         } else {
             resoult = massiv[(massiv.length + 1) / 2];
 
         }
        team.setMedianaMinus(resoult);
         return resoult;
     }
 
     @Override
     public double normalize(double min, double max, double current,
                             boolean isMax) {
         return 0;
     }
 
     @Override
     public double evaluateTeamRate(Team team, double indexRate,
                                    double unityRate, double plusRate, double minusRate) {
         return 0;
     }
 
     public void sort(int[] mass) {
         int tempo = 0;
         for (int i = 0; i < mass.length - 1; i++) {
             for (int j = i + 1; j < mass.length; j++) {
                 if (mass[i] > mass[j]) {
                     tempo = mass[j];
                     mass[j] = mass[i];
                     mass[i] = tempo;
                 }
             }
         }
 
     }
 }
