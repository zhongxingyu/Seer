 import org.junit.Test;
 
 import spiderman.Direction;
 import spiderman.NamedRelationship;
 import storage.BasicGraph;
 import storage.Graph;
 import storage.Node;
 import evaluator.Evaluable;
 
 public class DBTest {
 
 	@Test
 	public void test() 
 	{
 		Graph g = new BasicGraph(true);
 		
 		//create Neo
 		Person neo = new Person();
 		neo.setName("Neo");
 		neo.setAge(29);
 		
 		//create Trinity
 		Person trinity = new Person();
 		trinity.setName("Trinity");
 		
 		//create Morpheus
 		Person morpheus = new Person();
 		morpheus.setName("Morpheus");
 		morpheus.setOccupation("Total badass");
 		morpheus.setRank("Captain");
 		
 		//create Cypher
 		Person cypher = new Person();
 		cypher.setName("Cypher");
 		cypher.setLastName("Reagan");
 		
 		//create Agent Smith
 		Person smith = new Person();
 		smith.setName("Agent Smith");
 		
 		//create Architect
 		Person architect = new Person();
 		architect.setName("The Architect");
 		
 		Node<Person> neoNode = g.addNode(neo);
 		Node<Person> trinityNode = g.addNode(trinity);
 		Node<Person> morpheusNode = g.addNode(morpheus);
 		Node<Person> cypherNode = g.addNode(cypher);
 		Node<Person> smithNode = g.addNode(smith);
 		Node<Person> architectNode = g.addNode(architect);
 		
 		g.addRelationship(new NamedRelationship("LOVES"), Direction.Directed, trinityNode, neoNode);
 		g.addRelationship(new NamedRelationship("KNOWS"), Direction.Directed, morpheusNode, trinityNode);
 		g.addRelationship(new NamedRelationship("KNOWS"), Direction.Directed, neoNode, morpheusNode);
 		g.addRelationship(new NamedRelationship("KNOWS"), Direction.Directed, morpheusNode, cypherNode);
 		g.addRelationship(new NamedRelationship("KNOWS"), Direction.Directed, cypherNode, smithNode);
 		g.addRelationship(new NamedRelationship("CODED_BY"), Direction.Directed, smithNode, architectNode);
 		
 		FindPerson findArchitect = new FindPerson("The Architect", null, null, null);
 		
 		System.out.println(neoNode.breadthFirst(3).evaluate(findArchitect).size());
 		System.out.println(neoNode.breadthFirst(4).evaluate(findArchitect).size());
 		System.out.println(neoNode.breadthFirst(5).evaluate(findArchitect).size());
 	}
 
 	private class Person
 	{
 		private String name = "";
 		private String lastName = "";
 		private int age = 0;
 		private String occupation = "";
 		private String rank = "";
 		
 		public String getName() {  return name;}
 		public String getLastName() { return lastName;}
 		public int getAge() { return age;}
 		public String getOccupation() { return occupation;}
 		public String getRank() { return rank;}
 		
 		public void setName(String name) { this.name = name;}
 		public void setLastName(String lastName) { this.lastName = lastName;}
 		public void setAge(int age) { this.age = age;}
 		public void setOccupation(String occupation) { this.occupation = occupation;}
 		public void setRank(String rank) { this.rank = rank;}
 		
 		@Override
 		public boolean equals(Object o)
 		{
 			if (o instanceof Person)
 			{
 				Person p = (Person)o;
 				if (p.getName().equals(name) &&
 						p.getLastName().equals(lastName) &&
 						p.getAge()==age &&
 						p.getOccupation().equals(occupation) &&
 						p.getRank().equals(rank))
 				{
 					return true;
 				}
 			}
 			return false;
 		}
 	}
 	
 	private class FindPerson implements Evaluable
 	{
 		private String name = null;
 		private String lastName = null;
 		private int age = -1;
 		private String occupation = null;
 		private String rank = null;
 		
 		public FindPerson(String name, String lastName, int age, String occupation, String rank)
 		{
 			this.name = name;
 			this.lastName = lastName;
 			this.age = age;
 			this.occupation = occupation;
 			this.rank = rank;
 		}
 		
 		public FindPerson(String name, String lastName, String occupation, String rank)
 		{
 			this(name, lastName, -1, occupation, rank);
 		}
 
 		@Override
 		public <T> boolean evaluateNode(T data) {
 			Person p = null;
 			if (data instanceof Person)
 			{
 				p = (Person) data;
 			}
 			else
 			{
 				return false;
 			}
 			
 			if (name != null && !name.equals(p.getName()))
 			{
 				return false;
 			}
 			
 			if (lastName != null && !lastName.equals(p.getLastName()))
 			{
 				return false;
 			}
 			
 			if (age != -1 && age != p.getAge())
 			{
 				return false;
 			}
 			
 			if (occupation != null && !occupation.equals(p.getOccupation()))
 			{
 				return false;
 			}
 			
 			if (rank != null && !rank.equals(p.getRank()))
 			{
 				return false;
 			}
 			
 			return true;
 		}
 
 		@Override
 		public boolean evaluateNode(Node<?> data) {
 			return evaluateNode(data.getData());
 		}
 		
 	}
 }
