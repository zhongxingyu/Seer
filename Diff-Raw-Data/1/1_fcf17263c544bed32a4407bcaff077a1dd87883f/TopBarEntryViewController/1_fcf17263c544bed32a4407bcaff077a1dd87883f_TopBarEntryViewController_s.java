 package au.edu.unimelb.cis.dragons.core.controller;
 
 import au.edu.unimelb.cis.dragons.core.GameState;
 import react.ValueView;
 import tripleplay.ui.Group;
 import tripleplay.ui.Label;
 import tripleplay.ui.SizableGroup;
 import tripleplay.ui.layout.AxisLayout;
 import tripleplay.ui.layout.FlowLayout;
 
 /**
  * A ViewController that handles the presentation of information in the top
  * bar of the game. Shows a human readable description of the data and it's
  * value.
  * @author Aidan Nagorcka-Smith (aidanns@gmail.com)
  */
 public class TopBarEntryViewController extends ViewController {
 	
 	private GameState _gameState;
 	private GameState.Key _valueKey;
 	
 	private Label _titleLabel;
 	private Label _valueLabel;
 	
 	/**
 	 * Create a new entry based on the value stored under the @param valueKey
 	 * parameter in the game state.
 	 * @param gameState The game state object to pull data from.
 	 * @param valueKey The key of the value that will be displayed.
 	 */
 	public TopBarEntryViewController(GameState gameState, GameState.Key valueKey) {
 		_gameState = gameState;
 		_valueKey = valueKey;
 	}
 
 	@Override
 	public String title() {
 		return _gameState.shortDescriptionForKey(_valueKey);
 	}
 
 	@Override
 	protected Group createInterface() {
 		SizableGroup group = new SizableGroup(new FlowLayout());
 		group.setConstraint(AxisLayout.fixed());
 		_titleLabel = 
 				new Label(_gameState.shortDescriptionForKey(_valueKey) + ":");
 		group.add(_titleLabel);
 		_valueLabel =
 				new Label(_gameState.valueForKey(_valueKey).get().toString());
 		
 		_gameState.valueForKey(_valueKey).connect(new ValueView.Listener<Object>() {
 			@Override
 			public void onChange(Object value, Object oldValue) {
 				_valueLabel.text.update(value.toString());
 			}
 		});
 		return group;
 	}
 
 }
