 package org.ggll.grammar.validation;
 
 import ggll.core.list.ExtendedList;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.ggll.resource.CanvasResource;
 import org.ggll.syntax.graph.state.StateConnection;
 import org.ggll.syntax.graph.state.StateNode;
 import org.ggll.syntax.graph.state.StateHelper;
 
 public class TerminalValidation extends GrammarValidation
 {
 
 	private void alternativeNodes(StateNode node, ExtendedList<String> first, ExtendedList<StateNode> nodes)
 	{
 		StateNode alternative = StateHelper.findAlternativeNode(node);
 		while (alternative != null && !nodes.contains(alternative))
 		{
 			if (alternative.getType().equals(CanvasResource.N_TERMINAL))
 			{
 				StateNode leftSide = StateHelper.findLeftSide(alternative);
 				walkGraph(leftSide, first, nodes);
 			}
 			else if (alternative.getType().equals(CanvasResource.TERMINAL))
 			{
 				first.append(alternative.getTitle());
 			}
 			alternative = StateHelper.findAlternativeNode(alternative);
 		}
 	}
 
 	private void sucessorNodes(StateNode node, ExtendedList<String> first, ExtendedList<StateNode> nodes)
 	{
 		StateNode sucessor = StateHelper.findSucessorNode(node);
 		if (sucessor != null && !nodes.contains(sucessor))
 		{
 			if (sucessor.getType().equals(CanvasResource.N_TERMINAL))
 			{
				walkGraph(sucessor, first, nodes);
 			}
 			else if (sucessor.getType().equals(CanvasResource.TERMINAL))
 			{
 				first.append(sucessor.getTitle());
 			}
 		}
 	}
 
 	private void walkGraph(StateNode node, ExtendedList<String> first, ExtendedList<StateNode> nodes)
 	{
 		if (node == null)
 		{
 			return;
 		}
 		nodes.append(node);
 		sucessorNodes(node, first, nodes);
 		alternativeNodes(StateHelper.findSucessorNode(node), first, nodes);
 	}
 
 	@Override
 	public void validate()
 	{
 		for (final StateConnection connection : StateHelper.getSucessors().getAll())
 		{
 			StateNode node = StateHelper.findNode(connection.getSource());
 			ExtendedList<String> first = new ExtendedList<String>();
 			ExtendedList<StateNode> nodes = new ExtendedList<StateNode>();
 			walkGraph(node, first, nodes);
 			Set<String> unique = new HashSet<String>(first.getAll());
 			for (String key : unique)
 			{
 				if (Collections.frequency(first.getAll(), key) > 1)
 				{
 					addError("Duplicated terminal \"<a href='Label|" + key + "'>" + key + "</a>\" in a sequence of alternatives.", node);
 				}
 			}
 		}
 
 	}
 
 }
