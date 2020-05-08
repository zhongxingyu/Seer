 import csv.CSVWriter;
 
 import descriptors.Dominance;
 
 import java.util.*;
 import java.io.File;
 import java.io.IOException;
 
 import game.*;
 
 import map.MapGenerator;
 
 import td.TDLearning;
 import td.TDPlayer;
 
 import ui.terminal.TerminalUI;
 import ui.terminal.TerminalPlayer;
 
 public class TestGame 
 {
 	static public void main(String[] args) throws Exception
 	{
 		final TDLearning brain = new TDLearning();
 
 		if (args.length > 0 && new File(args[0]).exists())
 			brain.getNeuralNetwork().readWeights(new File(args[0]));
 
 		final TDPlayer tdPlayer = new TDPlayer("TD", brain);
 
 		List<Player> players = new Vector<Player>();
 		players.add(tdPlayer);
 		// players.add(new TDPlayer("TD 2", brain));
 		// players.add(new TDPlayer("TD 3", brain));
 		// players.add(new TDPlayer("TD 4", brain));
 		players.add(new RandomPlayer("Random"));
 		players.add(new SimplePlayer("Simple"));
 		// players.add(new DescriptorPlayer("Dominance", new Dominance()));
 
 		TerminalUI gui = new TerminalUI();
 
 		MapGenerator generator = new MapGenerator(players);
 
 		// Initialize scores table
 		final HashMap<Player, Integer> scores = new HashMap<Player, Integer>();
 		for (Player player : players)
 			scores.put(player, 0);
 
 		CSVWriter writer = new CSVWriter(System.out);
 		writer.write("Round");
 		writer.write(players);
 		writer.write("error");
 		writer.write("variance");
 		writer.endLine();
 
 		for (int i = 1; i <= 50000; ++i)
 		{
 			// Generate a random map
 			GameState state = generator.generate(4, 2.5);
 			
 			final Game game = new Game(players, state);
 			game.addEventListener(new GameEventAdapter() {
 				public void onTurnEnded(GameState state)
 				{
 					if (!state.getPlayers().contains(tdPlayer))
 					{
 						System.out.println("Stopping game because TDPlayer died");
 						game.stop();
 					}
 				}
 				
 				public void onGameEnded(GameState state)
 				{
 					Player winner = state.getCountries().get(0).getPlayer();
 					scores.put(winner, scores.get(winner) + 1);
 
 					// System.out.println("Winner: " + winner);
 				}
 			});
 			// game.addEventListener(gui);
 			game.run();
 
 			if (i % 100 == 0)
 			{
 				writer.write(i);
 				
 				for (Player player : players)
 					writer.write(scores.get(player));
 
 				writer.write(brain.getError().mean());
 				writer.write(brain.getError().variance());
 				writer.endLine();

				brain.getError().reset();
 			}
 		}
 
 		// Print total scores table
 		System.out.println("Total scores:");
 		for (Player player : players)
 			System.out.println("Player " + player + ": \t" + scores.get(player));
 
 		if (args.length > 0)
 			brain.getNeuralNetwork().writeWeights(new File(args[0]));
 	}
 }
