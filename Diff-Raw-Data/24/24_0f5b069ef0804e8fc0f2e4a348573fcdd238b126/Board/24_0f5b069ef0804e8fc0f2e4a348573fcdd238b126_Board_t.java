 package edu.selu.android.classygames.games.checkers;
 
 
 import java.util.List;
 import java.util.Map;
 
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import edu.selu.android.classygames.games.Coordinate;
 import edu.selu.android.classygames.games.GenericBoard;
 
 
 public class Board extends GenericBoard
 {
 
 
 	public final static byte BOARD_INVALID = -1;
 	public final static byte BOARD_NEW_GAME = 1;
 	public final static byte BOARD_NEW_MOVE = 2;
 	public final static byte BOARD_WIN = 15;
 
 	private final static byte LENGTH_HORIZONTAL = 8;
 	private final static byte LENGTH_VERTICAL = 8;
 
 
 	public Board()
 	{
 		super();
 
 		maxTeamSize = 12;
 	}
 
 
 	/**
 	 * Creates a Board object from JSON data.
 	 * 
 	 * @param boardJSONData
 	 * A String of JSON data that represents the Board object.
 	 */
 	@SuppressWarnings("unchecked")
 	public Board(final String boardJSONData)
 	{
 		this();
 
 		if (boardJSONData != null && !boardJSONData.isEmpty())
 		{
 			try
 			{
 				final Map<String, Object> data = (Map<String, Object>) new JSONParser().parse(boardJSONData);
 				final Map<String, Object> board = (Map<String, Object>) data.get("board");
 				final List<Object> teams = (List<Object>) board.get("teams");
 
 				final int teamsSize = teams.size();
 				for (byte i = 0; i < teamsSize; ++i)
 				{
 					parseTeamData((List<Object>) teams.get(i));
 				}
 			}
 			catch (final ParseException e)
 			{
 
 			}
 		}
 	}
 
 
 	@Override
 	public byte checkValidity()
 	{
 		byte userCount = 0;
 		byte challengedCount = 0;

 		for (byte x = 0; x < LENGTH_HORIZONTAL; ++x)
 		{
 			for (byte y = 0; y < LENGTH_VERTICAL; ++y)
 			{
 				//Check to see if position is in a legal spot in the first place. For example:
 				//[0,0] , [1,1] , [0,2] , [5,7] are all positions that a piece should never land on.
 				if (((y % 2 == 0) && (x % 2 == 0)) || ((y % 2 != 0) && (x % 2 != 0)))
 				{
 					if (getPosition(x, y).hasPiece())
 					{
 						return BOARD_INVALID;
 					}
 				}

 				//Check to make sure all pieces are in their proper place.
 				if (((y % 2 == 0) && (x % 2 != 0)) || ((y % 2 != 0) && (x % 2 == 0)))
 				{
 					if (y > 4)
 					{
 						//Check if piece is missing.
 						if (!getPosition(x, y).hasPiece())
 						{
 							return BOARD_INVALID;
 						}
 
 						//Make sure this piece belongs to the challenged team.
 						if (getPosition(x,y).getPiece().getTeam() != Piece.TEAM_CHALLENGED)
 						{
 							return BOARD_INVALID;
 						}
 
 						if (!getPosition(x,y).getPiece().isTypeNormal())
 						{
 							return BOARD_INVALID;
 						}
 
 						challengedCount++;
 					}
 
 					if (y < 2)
 					{
 						//Check if piece is missing.
 						if (!getPosition(x, y).hasPiece())
 						{
 							return BOARD_INVALID;
 						}
 
 						//Make sure this piece belongs to the user team.
 						if (getPosition(x,y).getPiece().getTeam() != Piece.TEAM_USER)
 						{
 							return BOARD_INVALID;
 						}
 
 						if (!getPosition(x,y).getPiece().isTypeNormal())
 						{
 							return BOARD_INVALID;
 						}
 
 						userCount++;
 					}
 					
 					if (y == 2)
 					{
 						if(getPosition(x,y).hasPiece())
 						{
 							//Make sure this piece belongs to the user team.
 							if (getPosition(x,y).getPiece().getTeam() != Piece.TEAM_USER)
 							{
 								return BOARD_INVALID;
 							}
 
 							if (!getPosition(x,y).getPiece().isTypeNormal())
 							{
 								return BOARD_INVALID;
 							}
 
 							userCount++;
 						}
 					}
 				}
 
 				//Only the first row should have a piece moved from a valid spot at first.
 				if ((y == 7) && (x % 2 == 0) || (y == 1) && (x % 2 == 0) || (y == 5) && (x % 2 == 0) || (y == 6) && (x % 2 != 0) || (y == 0) && (x % 2 != 0))
 				{
 					if (!getPosition(x,y).hasPiece())
 					{
 						return BOARD_INVALID;
 					}
 				}
 
 				//Make sure first move came from a valid piece.
 				if (y == 3)
 				{
 					if (x == 0)
 					{
 						//Check if piece has moved from farthest left.
 						if (getPosition(x, y).hasPiece())
 						{
 							if (getPosition((byte)(x+1),(byte)(y-1)).hasPiece())
 							{
 								return BOARD_INVALID;
 							}
 
 							userCount++;
 						}
 					}
 					//Check the rest of the pieces.
 					else if (x % 2 == 0)
 					{
 						if (getPosition(x,y).hasPiece())
 						{
 							if (getPosition((byte)(x+1),(byte)(y-1)).hasPiece() && getPosition((byte)(x-1),(byte)(y-1)).hasPiece())
 							{
 								return BOARD_INVALID;
 							}
 
 							if (!getPosition((byte)(x+1),(byte)(y-1)).hasPiece() && !getPosition((byte)(x-1),(byte)(y-1)).hasPiece())
 							{
 								return BOARD_INVALID;
 							}
 
 							userCount++;
 						}
 					}
 				}
 
 			}
 		}
 
 		if(challengedCount != maxTeamSize || userCount != maxTeamSize)
 		{
 			return BOARD_INVALID;
 		}
 
 		return BOARD_NEW_GAME;
 	}
 
 
 	@Override
 	public byte checkValidity(final String boardJSONData)
 	{
 		if (boardJSONData != null && !boardJSONData.isEmpty())
 		{
 			final Board boardNew = new Board(boardJSONData);
 			
 			byte userCount = 0;
 			byte challengedCount = 0;
 			byte challangedKings = 0;
 			byte userKings = 0;
 
 			for (byte x = 0; x < LENGTH_HORIZONTAL; ++x)
 			{
 				for (byte y = 0; y < LENGTH_VERTICAL; ++y)
 				{
 					//Check to see if position is in a legal spot in the first place. For example:
 					//[0,0] , [1,1] , [0,2] , [5,7] are all positions that a piece should never land on.
 					if (((y % 2 == 0) && (x % 2 == 0)) || ((y % 2 != 0) && (x % 2 != 0)))
 					{
 						if (boardNew.getPosition(x, y).hasPiece())
 						{
 							return BOARD_INVALID;
 						}
 					}
 
 					//Head count for each team - may need to implement something to subtract if jump is detected.
 					if (boardNew.getPosition(x, y).hasPiece())
 					{
 						if(boardNew.getPosition(x,y).getPiece().getTeam() == Piece.TEAM_CHALLENGED)
 						{
 							challengedCount++;
 						}
 						else
 						{
 							userCount++;
 						}
 					}
 
 					//TODO:Check if a piece moved from a previous position, then attempt to find piece in legal position
 					// probably best to check if enemy was in front first then if in the following board it got skipped
 					// or did the skipping ALSO be aware of pieces overlapping.
 					if (boardNew.getPosition(x, y).hasPiece() && !getPosition(x,y).hasPiece())
 					{
 						//Count number of new kings user made in one turn...no cheating only 1 new king per turn!!
 						if (boardNew.getPosition(x, y).getPiece().isTypeNormal() != true)
 						{
 							userKings++;
 						}
 					}
 				}
 			}
 
 			//Check for illegal amount of kings.
 			if (userKings > 1)
 			{
 				return BOARD_INVALID;
 			}
 
 			//WIN CONDITION: Check if challenged user is out of pieces.
 			if (challengedCount == 0)
 			{
 				return BOARD_WIN;
 			}
 
 			return BOARD_NEW_MOVE;
 		}
 		else
 		{
 			return BOARD_INVALID;
 		}
 	}
 
 
 	@Override
 	public Position getPosition(final byte x, final byte y)
 	{
 		if ((x >= 0 && x < LENGTH_HORIZONTAL) && (y >= 0 && y < LENGTH_VERTICAL))
 		{
 			return (Position) positions[x][y];
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 
 	@Override
 	protected void initializeBoard()
 	{
 		positions = new Position[LENGTH_HORIZONTAL][LENGTH_VERTICAL];
 
 		for (byte i = 0; i < LENGTH_HORIZONTAL; ++i)
 		{
 			for (byte j = 0; j < LENGTH_VERTICAL; ++j)
 			{
 				positions[i][j] = new Position();
 			}
 		}
 	}
 
 
 	@SuppressWarnings("unchecked")
 	private void parseTeamData(final List<Object> team)
 	{
 		final byte teamSize = (byte) team.size();
 
 		for (byte i = 0, currentTeamSize = 0; i < teamSize && currentTeamSize < maxTeamSize; ++i, ++currentTeamSize)
 		{
 			final Map<String, Object> piece = (Map<String, Object>) team.get(i);
 			final byte pieceType = Long.valueOf((Long) piece.get("type")).byteValue();
 			final byte pieceTeam = Long.valueOf((Long) piece.get("team")).byteValue();
 			final List<Long> unparsedCoordinate = (List<Long>) piece.get("coordinate");
 			
 			final byte x = unparsedCoordinate.get(0).byteValue();
 			final byte y = unparsedCoordinate.get(1).byteValue();
 
 			if ((x >= 0 && x < LENGTH_HORIZONTAL) && (y >= 0 && y < LENGTH_VERTICAL))
 			{
 				final Coordinate coordinate = new Coordinate(x, y);
 
 				if (!positions[coordinate.getX()][coordinate.getY()].hasPiece())
 				{
 					positions[coordinate.getX()][coordinate.getY()] = new Position(coordinate, pieceTeam, pieceType);
 				}
 			}
 		}
 	}
 
 
 	@SuppressWarnings("unchecked")
 	public static String flipTeams(final String board)
 	{
 		if (board != null && !board.isEmpty())
 		{
 			try
 			{
 				final Map<String, Object> data = (Map<String, Object>) new JSONParser().parse(board);
 				final Map<String, Object> boardData = (Map<String, Object>) data.get("board");
 				final List<Object> teamData = (List<Object>) boardData.get("teams");
 
 				// flip the teams
 				final Object team0 = teamData.get(0);
 				final Object team1 = teamData.get(1);
 				teamData.set(0, team1);
 				teamData.set(1, team0);
 
 				final byte teamsSize = (byte) teamData.size();
 
 				for (byte i = 0; i < teamsSize; ++i)
 				// loop through each team
 				{
 					final List<Object> team = (List<Object>) teamData.get(i);;
 					final byte teamSize = (byte) team.size();
 
 					for (byte j = 0; j < teamSize; ++j)
 					// flip the team's piece's coordinates
 					{
 						final Map<String, Object> piece = (Map<String, Object>) team.get(j);
 						final List<Long> coordinate = (List<Long>) piece.get("coordinate");
 						Byte x = coordinate.get(0).byteValue();
 						Byte y = coordinate.get(1).byteValue();
 						x = Byte.valueOf((byte) (LENGTH_HORIZONTAL - 1 - x));
 						y = Byte.valueOf((byte) (LENGTH_VERTICAL - 1 - y));
 
 						coordinate.set(0, x.longValue());
 						coordinate.set(1, y.longValue());
 					}
 
 					teamData.set(i, team);
 				}
 
 				return data.toString();
 			}
 			catch (final ParseException e)
 			{
 
 			}
 		}
 
 		return null;
 	}
 
 
 }
 
