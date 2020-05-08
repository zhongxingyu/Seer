 package org.lilian.experiment;
 
 import static org.lilian.util.Series.series;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.lilian.data.dimension.BiTakens;
 import org.lilian.data.real.AffineMap;
 import org.lilian.data.real.Datasets;
 import org.lilian.data.real.Generator;
 import org.lilian.data.real.Generators;
 import org.lilian.data.real.Histogram2D;
 import org.lilian.data.real.MVN;
 import org.lilian.data.real.Maps;
 import org.lilian.data.real.Point;
 import org.lilian.data.real.Similitude;
 import org.lilian.data.real.classification.Classification;
 import org.lilian.data.real.classification.Classified;
 import org.lilian.data.real.classification.Classifier;
 import org.lilian.data.real.classification.Classifiers;
 import org.lilian.data.real.fractal.IFS;
 import org.lilian.data.real.fractal.IFSClassifierBasic;
 import org.lilian.data.real.fractal.IFSs;
 import org.lilian.data.real.fractal.random.RIFSs;
 import org.lilian.grammars.Grammar;
 import org.lilian.grammars.TestGrammars;
 import org.lilian.graphs.DTGraph;
 import org.lilian.graphs.Graph;
 import org.lilian.search.Builder;
 import org.lilian.util.Series;
 
 /**
  * These functions manage the resources embedded in the Lilian library. 
  * 
  * Lilian has some datasets embedded in its jar, and it also has some methods 
  * for generating data automatically. These can be called from the init file.
  * @author Peter
  *
  */
 public class Resources
 {
 	
 	@Resource(name="sierpinski")
 	public static List<Point> sierpinski(@Name("size") int size)
 	{
 		List<Point> data = IFSs.sierpinski().generator().generate(size);
 		Collections.shuffle(data);
 		
 		return data;
 	}	
 	
 	@Resource(name="sierpinski-generator")
 	public static Generator<Point> sierpinskiGen(@Name("depth") int depth)
 	{
 		return IFSs.sierpinski().generator(depth);
 	}
 	
 	@Resource(name="cantor")
 	public static List<Point> cabntor(@Name("size") int size)
 	{
 		List<Point> data = IFSs.cantor().generator().generate(size);
 		Collections.shuffle(data);
 		
 		return data;
 	}	
 	
 	@Resource(name="cantor-generator")
 	public static Generator<Point> cantoriGen(@Name("depth") int depth)
 	{
 		return IFSs.cantor().generator(depth);
 	}
 
 	@Resource(name="sierpinski-noise")
 	public static List<Point> sierpinski(@Name("size") int size, @Name("noise") double noise)
 	{
 		if(noise == 0.0)
 			return sierpinski(size);
 		
 		List<Point> data = Datasets.addNoise(IFSs.sierpinski().generator(), noise).generate(size);
 		Collections.shuffle(data);
 		
 		return data;
 	}	
 	
 	@Resource(name="koch")
 	public static List<Point> koch(@Name("size") int size)
 	{
 		List<Point> data = IFSs.koch2Sim().generator().generate(size);
 		Collections.shuffle(data);
 		
 		return data;
 	}
 	
 	
 	@Resource(name="lorenz")
 	public static List<Point> lorenz(@Name("size") int size)
 	{
 		List<Point> data = Generators.lorenz().generate(size);
 		Collections.shuffle(data);
 		
 		return data;
 	}
 		
 	
 	
 	@Resource(name="sierpinski-off")
 	public static List<Point> sierpinskiOff(@Name("size") int size, @Name("p1") double p1, @Name("p2") double p2, @Name("p3") double p3)
 	{
 		List<Point> data = IFSs.sierpinskiOff(p1, p2, p3).generator().generate(size);
 		Collections.shuffle(data);
 		
 		return data;
 	}	
 	
 	@Resource(name="mandelbrot")
 	public static List<Point> mandelbrot(@Name("size") int size)
 	{
 		List<Point> data = Datasets.mandelbrot().generate(size);
 		
 		return data;
 	}
 	
 	@Resource(name="ball")
 	public static List<Point> ball(@Name("dim") int dim,  @Name("size") int size)
 	{
 		List<Point> data = Datasets.ball(dim).generate(size);
 		
 		return data;
 	}
 	
 	@Resource(name="cube")
 	public static List<Point> cube(@Name("dim") int dim,  @Name("size") int size)
 	{
 		List<Point> data = Datasets.cube(dim).generate(size);
 		
 		return data;
 	}
 	
 	@Resource(name="three")
 	public static List<Point> ball(@Name("size") int size)
 	{
 		List<Point> data = Datasets.three().generate(size);
 		
 		return data;
 	}	
 	
 	@Resource(name="random-ifs")
 	public static List<Point> randomIFS(@Name("dim") int dim, @Name("components") int components, @Name("size") int size, @Name("var") double var)
 	{
 		List<Point> data = IFSs.random(dim, components, var).generator().generate(size);
 		Collections.shuffle(data);
 		
 		return data;
 	}	
 	
 	@Resource(name="random-sim-ifs")
 	public static List<Point> randomSimIFS(@Name("dim") int dim, @Name("components") int components, @Name("size") int size, @Name("var") double var)
 	{
 		List<Point> data = IFSs.randomSimilitude(dim, components, var).generator().generate(size);
 		Collections.shuffle(data);
 		
 		return data;
 	}
 	
 	@Resource(name="by-parameters")
 	public static List<Point> byParameters(@Name("dim") int dim, @Name("components") int components, @Name("parameters") List<Double> parameters, @Name("size") int size)
 	{
 		Builder<IFS<Similitude>> builder = IFS.builder(components, Similitude.similitudeBuilder(dim));
 		IFS<Similitude> ifs = builder.build(parameters);
 		
 		List<Point> data = ifs.generator().generate(size);
 		Collections.shuffle(data);
 		
 		return data;
 	}	
 	
 	@Resource(name="sphere")
 	public static List<Point> sphere(@Name("dim") int dim,  @Name("size") int size)
 	{
 		List<Point> data = Datasets.sphere(dim).generate(size);
 		
 		return data;
 	}	
 	
 	@Resource(name="swiss")
 	public static List<Point> swiss(@Name("size") int size, @Name("noise") double noise)
 	{
 		return Datasets.swiss(noise).generate(size);
 	}
 	
 	@Resource(name="swiss-generator")
 	public static Generator<Point> swissGen( @Name("noise") double noise)
 	{
 		return Datasets.swiss(noise);
 	}
 	
 	@Resource(name="spiral")
 	public static List<Point> spiral(@Name("size") int size, @Name("noise") double noise)
 	{
 		if(noise == 0.0)
 			return Datasets.spiral(-45, 45).generate(size);
 		
 		return Datasets.addNoise(Datasets.spiral(-45, 45), noise).generate(size);
 	}	
 	
 
 	@Resource(name="mvn")
 	public static List<Point> mvn(@Name("dim") int dim,  @Name("size") int size)
 	{
 		Generator<Point> gen = new MVN(dim);
 		List<Point> data = gen.generate(size);
 		
 		return data;
 	}	
 	
 	@Resource(name="mvn var")
 	public static List<Point> mvn(@Name("dim") int dim, @Name("var") double var, @Name("size") int size)
 	{
 		Generator<Point> gen = new MVN(dim, var);
 		List<Point> data = gen.generate(size);
 		
 		return data;
 	}		
 	
 	@Resource(name="mandelbrot-class")
 	public static Classified<Point> mandelbrotClass(@Name("size") int size)
 	{
 		List<Point> points = new MVN(2, 10.0).generate(size);
 		List<Integer> classes = Classifiers.mandelbrot().classify(points);
 		
 		return Classification.combine(points, classes);
 	}		
 	
 	@Resource(name="newton")
 	public static Classified<Point> newton(@Name("size") int size)
 	{
 		List<Point> points = Datasets.cube(2).generate(size);
 		List<Integer> classes = Classifiers.newton().classify(points);
 		
 		return Classification.combine(points, classes);
 	}	
 	
 	
 	@Resource(name="sierpinski classified")
 	public static Classified<Point> sierpinski(@Name("size") int size, @Name("depth") int depth)
 	{
 		List<Point> points = IFSs.sierpinskiOffSim(1.0, 1.0, 1.0).generator().generate(size);
 		
 		return Classification.combine(points, IFSClassifierBasic.sierpinski(depth).classify(points));
 	}
 	
 	@Resource(name="rossler")
 	public static List<Point> rossler(@Name("size") int size)
 	{
 		return Generators.rossler().generate(size);
 	}		
 	
 	@Resource(name="logistic")
 	public static List<Point> logisticr(@Name("size") int size)
 	{
 		return Generators.logistic().generate(size);
 	}			
 	
 	@Resource(name="henon")
 	public static List<Point> henon(@Name("size") int size)
 	{
 		return Generators.fromMap(Maps.henon(), new Point(2)).generate(size);
 	}		
 	
 	@Resource(name="magnet")
 	public static Classified<Point> magnet(@Name("size") int size)
 	{
 		List<Point> points = new MVN(2).generate(size);
 		List<Integer> classes = Classifiers.magnet().classify(points);
 		
 		return Classification.combine(points, classes);
 	}
 	
 	@Resource(name="square")
 	public static Classified<Point> square(@Name("size") int size, @Name("dim")int dim, @Name("r")double r)
 	{
 		List<Point> points = Datasets.cube(dim).generate(size);
 		List<Integer> classes = Classifiers.square(dim, r).classify(points);
 		
 		return Classification.combine(points, classes);
 	}	
 	
 	@Resource(name="line")
 	public static Classified<Point> line(@Name("size") int size, @Name("dim")int dim)
 	{
 		List<Point> points = new MVN(dim, 1.0).generate(size);
 		List<Integer> classes = Classifiers.line(dim).classify(points);
 		
 		return Classification.combine(points, classes);
 	}
 	
 	@Resource(name="sine")
 	public static Classified<Point> sine(@Name("size") int size)
 	{
 		List<Point> points = new MVN(2, 0.2).generate(size);
 		List<Integer> classes = Classifiers.sine().classify(points);
 		
 		return Classification.combine(points, classes);
 	}			
 	
 	@Resource(name="newton-points")
 	public static List<Point> newtonPoints(@Name("size") int size)
 	{
 		List<Point> points = new ArrayList<Point>(size);
 		
 		Generator<Point> gen = new MVN(2, 3.0);
 		Classifier cls = Classifiers.newton();
 		for(int i : Series.series(size))
 		{
 			int c;
 			Point p;
 			do {
 				p = gen.generate();
 				c = cls.classify(p);
 			} while(c != 0);
 			
 			points.add(p);
 		}
 				
 		return points;
 	}
 	
 	@Resource(name="rdf graph")
 	public static DTGraph<String, String> rdfGraph(
 			@Name("file") File file)
 	{
 		return org.lilian.graphs.data.RDF.read(file);
 	}
 	
 	@Resource(name="gml graph")
 	public static Graph<String> gmlGraph(@Name("file") File file) 
 		throws IOException
 	{
 		return org.lilian.graphs.data.GML.read(file);	
 	}
 	
 //	@Resource(name="text graph")
 //	public static Graph<Vertex<String>, Edge<String>> txtGraph(
 //			@Name("file") File file, 
 //			@Name("directed") boolean directed) 
 //		throws IOException
 //	{
 //		return org.data2semantics.tools.graphs.Graphs.graphFromTSV(file);	
 //	}
 //
 //	@Resource(name="line graph")
 //	public static Graph<Integer, Integer> lineGraph(
 //			@Name("file") File file) 
 //		throws IOException
 //	{
 //		return org.data2semantics.tools.graphs.Graphs.singLine(file);
 //	}
 //	
 //	@Resource(name="integer graph") 
 //	public static Graph<Vertex<Integer>, Edge<Integer>> txtIntegerGraph(
 //			@Name("file") File file, 
 //			@Name("directed")boolean directed) 
 //		throws IOException
 //	{
 //		return org.data2semantics.tools.graphs.Graphs.intDirectedGraphFromTSV(file);	
 //	}
 //	
 //	@Resource(name="random graph")
 //	public static Graph<Integer, Integer> random(
 //		@Name("number of nodes") int nodes,
 //		@Name("edge probability") double edgeProb)
 //	{
 //		return Graphs.random(nodes, edgeProb);
 //	}
 //	
 //	@Resource(name="ba random graph")
 //	public static Graph<Integer, Integer> abRandom(
 //		@Name("number of nodes") int nodes,
 //		@Name("number to attach") int toAttach)
 //	{
 //		return Graphs.abRandom(nodes, 3, toAttach);
 //	}
 //	
 //	@Resource(name="random graph lilian")
 //	public static org.lilian.util.graphs.old.BaseGraph<String> randomLilian(
 //		@Name("number of nodes") int nodes,
 //		@Name("edge probability") double edgeProb)
 //	{
 //		return org.lilian.util.graphs.old.Graphs.random(nodes, edgeProb);
 //	}
 //	
 //	@Resource(name="ba random graph lilian")
 //	public static org.lilian.util.graphs.old.BaseGraph<String> abRandomLilian(
 //		@Name("number of nodes") int nodes,
 //		@Name("number to attach") int toAttach)
 //	{
 //		return org.lilian.util.graphs.old.Graphs.ba(nodes, 3, toAttach);
 //	}
 //	
 	
 	@Resource(name="csv file")
 	public static List<Point> csvFile(@Name("file") File file) 
 		throws IOException
 	{
 		return Datasets.readCSV(file);
 	}
 	
 	@Resource(name="csv classification")
 	public static Classified<Point> csvClassification(@Name("file") File file) 
 		throws IOException
 	{
 		return Classification.readCSV(file);
 	}
 	
 	@Resource(name="csv classification filtered")
 	public static List<Point> csvClassificationFiltered(@Name("file") File file, @Name("class") int clss) 
 		throws IOException
 	{
 		return Classification.readCSV(file).points(clss);
 	}	
 	
 	@Resource(name="read images")
 	public static List<Point> readImages(@Name("dir") File dir, @Name("gray") boolean gray)
 		throws IOException
 	{
 		return Datasets.readImages(dir, gray);
 	}
 	
 	@Resource(name="image to points")
 	public static List<Point> imageToPoints(@Name("file") File file, @Name("size") int size)
 			throws IOException
 	{
 		Histogram2D hist = Histogram2D.fromImage(file);
 		return hist.generate(size);
 	}
 	
 	@Resource(name="double takens")
 	public static List<Double> doubleTakens(
 			@Name("size") int size,
 			@Name("d1") double d1, 
 			@Name("d2") double d2, 
 			@Name("split") double split, 
 			@Name("max") double max)
 	{
 		List<Double> gen = new BiTakens(split, max, d1, d2).generate(size);
 		Collections.sort(gen);
 		
 		return gen;
 	}
 	
 	@Resource(name="toy grammar")
 	public static Grammar<String> toyGrammar(@Name("name") String name)
 	{
 		if(name.equals("av"))
 			return TestGrammars.adriaansVervoort();
 		if(name.equals("ta1"))
 			return TestGrammars.ta1();
 		if(name.equals("mirror"))
 			return TestGrammars.mirror();
 
 		throw new IllegalArgumentException("Grammar \""+name+"\" not known.");
 	}	
 	
 	@Resource(name="toy grammar data")
 	public static List<List<String>> toyGrammarData(@Name("name") String name, @Name("size")int size)
 	{
 		Grammar<String> grammar = toyGrammar(name);
 		System.out.println(grammar);
 		List<List<String>> data = new ArrayList<List<String>>(size);
 		for(int i : series(size))
 			data.add(grammar.generateSentence("S", 0, 25));
 		
 		return data;
 	}
 	
 	@Resource(name="file")
 	public static File file(@Name("file") String file)
 			throws IOException
 	{
 		return new File(file);
 	}
 	
 	@Resource(name="cantor RIFS")
 	public static List<List<Point>> cantorRIFS(@Name("num sets") int numSets, @Name("per set") int perSet, @Name("depth") int depth)
 	{
 		List<List<Point>> data = new ArrayList<List<Point>>(numSets);
 		
 		for(int i : series(numSets))
 			data.add(RIFSs.cantor().randomInstance(perSet, depth));
 	
 		return data;
 	}
 	
 	@Resource(name="koch RIFS")
 	public static List<List<Point>> kochRIFS(@Name("num sets") int numSets, @Name("per set") int perSet, @Name("depth") int depth)
 	{
 		List<List<Point>> data = new ArrayList<List<Point>>(numSets);
 		
 		for(int i : series(numSets))
 			data.add(RIFSs.koch2UpDown().randomInstance(perSet, depth));
 	
 		return data;
 	}
 	
 	@Resource(name="koch RIFS mean")
 	public static List<Point> kochRIFSMean(@Name("size") int size, @Name("depth") int depth)
 	{
 		List<Point> data = RIFSs.koch2UpDown().meanInstance(size, depth);
 	
 		return data;
 	}
 	
 	@Resource(name="sierpinski RIFS")
 	public static List<List<Point>> sierpinskiRIFS(@Name("num sets") int numSets, @Name("per set") int perSet, @Name("depth") int depth)
 	{
 		List<List<Point>> data = new ArrayList<List<Point>>(numSets);
 		
 		for(int i : series(numSets))
 			data.add(RIFSs.sierpinski().randomInstance(perSet, depth));
 	
 		return data;
 	}
 }
 
 
