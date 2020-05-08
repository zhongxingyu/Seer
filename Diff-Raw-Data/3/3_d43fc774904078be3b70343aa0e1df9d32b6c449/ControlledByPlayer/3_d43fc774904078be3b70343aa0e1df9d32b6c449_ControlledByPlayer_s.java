 package aigilas.strategies.impl;
 
 import java.util.Arrays;
 import java.util.List;
 
 import spx.bridge.ActorType;
 import spx.core.Point2;
 import spx.io.Contexts;
 import spx.io.Input;
 import aigilas.creatures.ICreature;
 import aigilas.creatures.Stats;
 import aigilas.management.Commands;
 import aigilas.strategies.IStrategy;
 import aigilas.strategies.Strategy;
 
 public class ControlledByPlayer extends IStrategy {
 	private boolean _isCasting = false;
 	private Point2 _keyVelocity = new Point2(0, 0);
 
 	private static List<Commands> __hotkeys = Arrays.asList(Commands.HotSkill1, Commands.HotSkill2, Commands.HotSkill3);
 
 	public ControlledByPlayer(ICreature parent) {
 		super(parent, Strategy.ControlledByPlayer);
 
 		_targets.AddTargetTypes(ActorType.NONPLAYER);
 	}
 
 	@Override
 	public void Act() {
 		if (Input.IsActive(Commands.Start, _parent.GetPlayerIndex())) {
 			_parent.SetPlaying(true);
 		}
 		if (Input.IsActive(Commands.Back, _parent.GetPlayerIndex())) {
 			_parent.SetPlaying(false);
 		}
 		if (_parent.IsPlaying()) {
 			if (!Input.IsContext(Contexts.Inventory, _parent.GetPlayerIndex())) {
 				float leftVelocity = (Input.IsActive(Commands.MoveLeft, _parent.GetPlayerIndex()) ? -Stats.DefaultMoveDistance : 0);
 				float rightVelocity = ((Input.IsActive(Commands.MoveRight, _parent.GetPlayerIndex())) ? Stats.DefaultMoveDistance : 0);
 				_keyVelocity.SetX(rightVelocity + leftVelocity);
 
 				float downVelocity = ((Input.IsActive(Commands.MoveDown, _parent.GetPlayerIndex())) ? -Stats.DefaultMoveDistance : 0);
 				float upVelocity = ((Input.IsActive(Commands.MoveUp, _parent.GetPlayerIndex())) ? Stats.DefaultMoveDistance : 0);
 				_keyVelocity.SetY(upVelocity + downVelocity);
 
 				if (Input.IsContext(Contexts.Free, _parent.GetPlayerIndex())) {
 					boolean isPress = Input.IsActive(Commands.Confirm, _parent.GetPlayerIndex());
 					if (!isPress) {
 						_parent.SetInteraction(false);
 					}
 					if (isPress && !_parent.IsInteracting()) {
 						_parent.SetInteraction(true);
 					}
 					int skillCycleVelocity = ((Input.IsActive(Commands.CycleLeft, _parent.GetPlayerIndex())) ? -1 : 0) + ((Input.IsActive(Commands.CycleRight, _parent.GetPlayerIndex())) ? 1 : 0);
 					_parent.CycleActiveSkill(skillCycleVelocity);
 
 					if (!_isCasting) {
 						if (!Input.IsActive(Commands.Confirm, _parent.GetPlayerIndex(), false)) {
 							_parent.MoveIfPossible(_keyVelocity.X, _keyVelocity.Y);
 						}
 						if (!_keyVelocity.IsZero()) {
 							_parent.SetSkillVector(_keyVelocity);
 						}
 					}
 				}
 				if (Input.IsActive(Commands.Skill, _parent.GetPlayerIndex())) {
 					_isCasting = true;
 				}
 
 				for (Commands hotkey : __hotkeys) {
 					if (Input.IsActive(hotkey, _parent.GetPlayerIndex())) {
 						if (!Input.IsActive(Commands.LockSkill, _parent.GetPlayerIndex(), false)) {
 							if (_parent.SetHotSkillActive(hotkey)) {
 								_isCasting = true;
 							}
 						}
 						else {
 							_parent.MarkHotSkill(hotkey);
 						}
 					}
 				}
 
 				if (_isCasting) {
 					if (_parent.GetSkillVector() == null) {
 						_parent.SetSkillVector(new Point2(1, 0));
 					}
 					if (!_parent.GetSkillVector().IsZero()) {
 						_parent.UseActiveSkill();
						_isCasting = false;
 					}
 				}
 			}
 
 			if (Input.IsActive(Commands.Inventory, _parent.GetPlayerIndex())) {
 				Input.SetContext(_parent.ToggleInventoryVisibility() ? Contexts.Inventory : Contexts.Free, _parent.GetPlayerIndex());
 			}
 		}
 	}
 }
