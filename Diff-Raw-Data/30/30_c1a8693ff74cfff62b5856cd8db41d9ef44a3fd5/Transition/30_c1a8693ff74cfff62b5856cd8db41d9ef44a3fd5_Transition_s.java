 package se705.bisimulation.lts;
 
 import java.util.Arrays;
 
public class Transition {
 	public final String source;
 	public final String action;
 	public final String destination;
 
 	public Transition(final String source, final String action, final String destination) {
 		this.source = source;
 		this.action = action;
 		this.destination = destination;
 	}
 
 	@Override
 	public int hashCode() {
 		return Arrays.hashCode(new String[] {this.source, this.action, this.destination});
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 		Transition other = (Transition) obj;
 		return this.source.equalsIgnoreCase(other.source)
 				&& this.action.equalsIgnoreCase(other.action)
 				&& this.destination.equalsIgnoreCase(other.destination);
 	}
 
 	@Override
 	public String toString() {
 		return String.format("(state(%s),action(%s),state(%s))", this.source.substring(1), this.action, this.destination.substring(1));
 	}
 }
