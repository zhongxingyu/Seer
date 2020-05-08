 package com.ternovsky;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ternovsky
  * Date: 24.01.13
  * Time: 23:59
  * To change this template use File | Settings | File Templates.
  */
 public class Conformation {
 
     private List<Particle> particles;
     private double energy;
     private double parameter;
 
     public List<Particle> getParticles() {
         return particles;
     }
 
     public void setParticles(List<Particle> particles) {
         this.particles = particles;
     }
 
     public double getParameter() {
         return parameter;
     }
 
     public void setParameter(double parameter) {
         this.parameter = parameter;
     }
 
     public double getEnergy() {
         for (int i = 0; i < particles.size(); i++) {
             for (int j = i + 1; j < particles.size(); j++) {
                 double r = getDistance(particles.get(i), particles.get(j));
                 energy += Math.pow(Math.E, parameter * (1 - r)) * (Math.pow(Math.E, parameter * (1 - r)) - 2);
             }
         }
 
         return energy;
     }
 
     private double getDistance(Particle particle1, Particle particle2) {
         double dx2 = Math.pow(particle1.x - particle2.x, 2);
         double dy2 = Math.pow(particle1.y - particle2.y, 2);
         double dz2 = Math.pow(particle1.z - particle2.z, 2);
         return Math.pow(dx2 + dy2 + dz2, 0.5);
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("Parameter = ");
         builder.append(parameter);
         builder.append("\n");
 
        String format = "%+.14f";
         for (Particle particle : particles) {
             builder.append(String.format(format, particle.x));
             builder.append("\t");
             builder.append(String.format(format, particle.y));
             builder.append("\t");
             builder.append(String.format(format, particle.z));
             builder.append("\n");
         }
 
         builder.append("Energy = ");
         builder.append(energy);
         builder.append("\n");
         builder.append("\n");
 
         return builder.toString();
     }
 }
