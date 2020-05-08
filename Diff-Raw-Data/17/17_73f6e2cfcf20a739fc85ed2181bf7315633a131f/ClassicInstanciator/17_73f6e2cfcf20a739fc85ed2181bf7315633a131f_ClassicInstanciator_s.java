 package ngenes2;
 
 import java.util.Random;
 import ngenes2.evolver.ClassicEvolver;
 import ngenes2.evolver.Evolver;
 import ngenes2.individual.generator.Generator;
 import ngenes2.ops.crossover.Crossover;
 import ngenes2.ops.mutator.Mutator;
 import ngenes2.population.BasicPopulation;
 import ngenes2.population.Population;
 import ngenes2.population.PopulationFactory;
 import org.picocontainer.Characteristics;
 import org.picocontainer.DefaultPicoContainer;
 
 public class ClassicInstanciator {
 
     private final DefaultPicoContainer pico;
 
     public ClassicInstanciator() {
         pico = new DefaultPicoContainer();
     }
 
     public <T> ClassicInstanciator with( Class<T> klass ) {
         pico.addComponent(klass);
         return this;
     }
 
     public ClassicInstanciator with( Object o ) {
         pico.addComponent(o);
         return this;
     }
 
     private void create() {
         pico.addComponent(new Random());
         pico.addComponent(Generator.class);
         pico.addComponent(BasicPopulation.Factory.class);
         pico.addComponent(Mutator.class);
         pico.addComponent(Crossover.class);
         pico.addComponent(ClassicEvolver.class);
         pico.start();
 
     }
 
    public Population run( int generations ) {
         create();
         Generator gen = pico.getComponent(Generator.class);
        Population pop = pico.getComponent(PopulationFactory.class).create(gen, generations );
         Evolver e = pico.getComponent(Evolver.class);
         e.evolve(pop);
         return pop;
     }
 }
