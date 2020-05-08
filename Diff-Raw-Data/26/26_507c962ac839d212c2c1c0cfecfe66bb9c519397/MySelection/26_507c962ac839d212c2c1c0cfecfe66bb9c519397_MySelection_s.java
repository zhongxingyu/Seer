public class MySelection extends Selection 
{
    public void initialize(int popsize) 
    {
     this.popsize = popsize;
    }
 
    public Individual[] select(Individual[] population) 
    {
     Individual[] newPopulation = new Individual[popsize];
 
     //  Code for your selection operator ...
 
     return newPopulation;
    }
 }
