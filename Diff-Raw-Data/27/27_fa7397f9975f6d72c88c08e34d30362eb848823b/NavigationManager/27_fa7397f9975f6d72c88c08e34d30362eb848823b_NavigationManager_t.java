 package navigation;
 
 import java.util.ArrayList;
 
 import org.lwjgl.util.Point;
 import org.newdawn.slick.Graphics;
 
 import entity.BasicEntity;
 import entity.EntityHandler;
 import events.Event;
 import player.Player;
 
 public class NavigationManager {
 	private EntityHandler entitys;
 	private ArrayList<ArrayList<Boolean>> navMesh;
 	private int mapWidth, mapHeight;
 	private ArrayList<Player> playerList;
 	private ArrayList<Event> playerEvents;
 	private ArrayList<ArrayList<Point>> playerPathes;
 	private final int GRID_SIZE = 20;
 
 	public NavigationManager(EntityHandler entitys, int mapWidth, int mapHeight) {
 		this.entitys = entitys;
 		this.navMesh = new ArrayList<>();
 		this.mapWidth = mapWidth;
 		this.mapHeight = mapHeight;
 		this.playerList = new ArrayList<>();
 		this.playerPathes = new ArrayList<>();
 		this.playerEvents = new ArrayList<>();
 
 		createNavMesh();
 	}
 
 	public void addMovement(Player player, Event event, int x, int y) {
 		ArrayList<Point> path = findPath(new Point(player.getFootX()
 				/ GRID_SIZE, player.getFootY() / GRID_SIZE), new Point(
 				(x + GRID_SIZE / 2) / GRID_SIZE, (y + GRID_SIZE / 2)
 						/ GRID_SIZE));
 		if (this.playerList.indexOf(player) != -1) {
 			this.playerPathes.set(this.playerList.indexOf(player), path);
 			this.playerEvents.set(this.playerList.indexOf(player), event);
 		} else if (path.size() > 1) {
 			this.playerList.add(player);
 			this.playerPathes.add(path);
 			this.playerEvents.add(event);
 		}
 	}
 
 	public void update() {
 		for (int i = 0; i < playerList.size(); i++) {
 			Player player = playerList.get(i);
 
 			if (player.getFootX() == playerPathes.get(i).get(0).getX()
 					* GRID_SIZE
 					&& player.getFootY() == playerPathes.get(i).get(0).getY()
 							* GRID_SIZE) {
 				this.playerPathes.get(i).remove(0);
 
 				if (this.playerPathes.get(i).isEmpty()) {
 
 					if (this.playerEvents.get(i) != null) {
 						this.playerEvents.get(i).activateEvent(
 								this.playerList.get(i));
 					}
 					this.playerList.remove(i);
 					this.playerPathes.remove(i);
 					this.playerEvents.remove(i);
 
 					player.releaseDirection(Player.RIGHT);
 					player.releaseDirection(Player.LEFT);
 					player.releaseDirection(Player.UP);
 					player.releaseDirection(Player.DOWN);
 
 					break;
 				}
 			}
 
 			if (player.getFootX() > playerPathes.get(i).get(0).getX()
 					* GRID_SIZE) {
 				player.setDirection(Player.LEFT);
 			} else if (player.getFootX() < playerPathes.get(i).get(0).getX()
 					* GRID_SIZE) {
 				player.setDirection(Player.RIGHT);
 			} else if (player.getFootX() == playerPathes.get(i).get(0).getX()
 					* GRID_SIZE) {
 				player.releaseDirection(Player.RIGHT);
 				player.releaseDirection(Player.LEFT);
 			}
 
 			if (player.getFootY() > playerPathes.get(i).get(0).getY()
 					* GRID_SIZE) {
 				player.setDirection(Player.UP);
 			} else if (player.getFootY() < playerPathes.get(i).get(0).getY()
 					* GRID_SIZE) {
 				player.setDirection(Player.DOWN);
 			} else if (player.getFootY() == playerPathes.get(i).get(0).getY()
 					* GRID_SIZE) {
 				player.releaseDirection(Player.UP);
 				player.releaseDirection(Player.DOWN);
 			}
 		}
 	}
 
 	private void createNavMesh() {
 
 		for (int i = 0; i < mapWidth / GRID_SIZE; i++) {
 			ArrayList<Boolean> row = new ArrayList<>();
 			for (int j = 0; j < mapHeight / GRID_SIZE; j++) {
 				if (entitys.getEntity(i * GRID_SIZE, j * GRID_SIZE) != null
 						|| i < 3 || j < 5 || i > mapWidth / GRID_SIZE - 3
 						|| j > mapHeight / GRID_SIZE - 3) {
 					row.add(false);
 				} else {
 					row.add(true);
 				}
 			}
 			navMesh.add(row);
 		}
 	}
 
 	public void rendermesh(Graphics g) {
 		for (int i = 0; i < navMesh.size(); i++) {
 			for (int j = 0; j < navMesh.get(i).size(); j++) {
 				if (navMesh.get(i).get(j)) {
 					g.fillRect(i * GRID_SIZE, j * GRID_SIZE, 2, 2);
 				}
 			}
 		}
 		if (!this.playerPathes.isEmpty()) {
 			for (int i = 0; i < playerPathes.get(0).size(); i++) {
 				g.fillRect(playerPathes.get(0).get(i).getX() * GRID_SIZE,
 						playerPathes.get(0).get(i).getY() * GRID_SIZE, 5, 5);
 			}
 		}
 	}
 
 	private ArrayList<Point> findPath(Point start, Point end) {
 		int[][] area = new int[this.navMesh.size()][this.navMesh.get(0).size()];
 
 		for (int i = 0; i < area.length; i++) {
 			for (int j = 0; j < area[0].length; j++) {
 				area[i][j] = 500;
 			}
 		}
 		area[start.getX()][start.getY()] = 1;
 
 		if (end.getX() < 3) {
 			end.setX(3);
 		}
 		if (end.getX() > navMesh.size() - 3) {
 			end.setX(navMesh.size() - 3);
 		}
 		if (end.getY() < 5) {
 			end.setY(5);
 		}
 		if (end.getY() > navMesh.get(0).size() - 3) {
 			end.setY(navMesh.get(0).size() - 3);
 		}
 
 		end = getRealEnd(end);
 
 		createArea(area, start, end, 100);
 		return createPath(area, start, end);
 	}
 
 	private Point getRealEnd(Point end) {
 		if (navMesh.get(end.getX()).get(end.getY())) {
 			return end;
 		}
 		Point realEnd = null;
 
 		BasicEntity newEntity = entitys.getEntity(end.getX() * GRID_SIZE,
 				end.getY() * GRID_SIZE);
 		int leftDistance = Math.abs(newEntity.getX() - end.getX() * GRID_SIZE);
 		int rightDistance = Math.abs(newEntity.getX() + newEntity.getWidth()
 				- end.getX() * GRID_SIZE);
 		int downDistance = Math.abs(newEntity.getY() + newEntity.getHeight()
 				- end.getY() * GRID_SIZE);
 		int upDistance = Math.abs(newEntity.getY() + newEntity.getHeight() / 2
 				- end.getY() * GRID_SIZE);
 
 		if (leftDistance <= rightDistance && leftDistance <= downDistance
 				&& leftDistance <= upDistance) {
 			realEnd = new Point((newEntity.getX() - GRID_SIZE / 2) / GRID_SIZE,
 					end.getY());
		} else if (rightDistance <= leftDistance && rightDistance <= downDistance
				&& rightDistance <= upDistance) {
 			realEnd = new Point(
 					(newEntity.getX() + GRID_SIZE + newEntity.getWidth())
 							/ GRID_SIZE, end.getY());
		} else if (downDistance <= rightDistance && downDistance <= leftDistance
				&& downDistance <= upDistance) {
 			realEnd = new Point(end.getX(),
 					(newEntity.getY() + GRID_SIZE + newEntity.getHeight())
 							/ GRID_SIZE);
 		} else if (upDistance < leftDistance && upDistance < downDistance
 				&& upDistance < rightDistance) {
 			realEnd = new Point(end.getX(),
					(newEntity.getY() - GRID_SIZE + newEntity.getHeight() / 2) 
							/ GRID_SIZE);
 		}
 
 		return realEnd;
 	}
 
 	private ArrayList<Point> createPath(int[][] area, Point start, Point end) {
 		ArrayList<Point> path = new ArrayList<Point>();
 		ArrayList<Point> neighbours = getNeighbours(end);
 		int lowestValue;
 		Point lowestPoint;
 
 		lowestValue = area[neighbours.get(0).getX()][neighbours.get(0).getY()];
 		lowestPoint = new Point(neighbours.get(0).getX(), neighbours.get(0)
 				.getY());
 		path.add(end);
 		while (!(start.getX() == end.getX() && start.getY() == end.getY())
 				&& lowestValue != 1) {
 			neighbours = getNeighbours(lowestPoint);
 			for (int i = 0; i < neighbours.size(); i++) {
 				if (area[neighbours.get(i).getX()][neighbours.get(i).getY()] < lowestValue) {
 					lowestPoint = new Point(neighbours.get(i).getX(),
 							neighbours.get(i).getY());
 					lowestValue = area[neighbours.get(i).getX()][neighbours
 							.get(i).getY()];
 				}
 			}
 			path.add(0, lowestPoint);
 		}
 		path.add(0, start);
 		return path;
 	}
 
 	private void createArea(int[][] area, Point start, Point end, int deathTime) {
 		if (deathTime == 0) {
 			return;
 		}
 
 		ArrayList<Point> neighbours = getDirectNeighbours(start);
 
 		for (int i = 0; i < neighbours.size(); i++) {
 			if (navMesh.get(neighbours.get(i).getX()).get(
 					neighbours.get(i).getY())) {
 				if (area[neighbours.get(i).getX()][neighbours.get(i).getY()] > area[start
 						.getX()][start.getY()] + 1) {
 					area[neighbours.get(i).getX()][neighbours.get(i).getY()] = area[start
 							.getX()][start.getY()] + 1;
 					if (neighbours.get(i).getX() == end.getX()
 							&& neighbours.get(i).getY() == end.getY()) {
 						return;
 					} else {
 						createArea(area, new Point(neighbours.get(i).getX(),
 								neighbours.get(i).getY()), end, deathTime - 1);
 					}
 				}
 			}
 		}
 	}
 
 	private ArrayList<Point> getDirectNeighbours(Point point) {
 		ArrayList<Point> newArea = new ArrayList<>();
 		if (point.getX() - 1 > 0) {
 			newArea.add(new Point(point.getX() - 1, point.getY()));
 		}
 		if (point.getY() - 1 > 0) {
 			newArea.add(new Point(point.getX(), point.getY() - 1));
 		}
 		if (point.getX() + 1 < navMesh.size() - 1) {
 			newArea.add(new Point(point.getX() + 1, point.getY()));
 		}
 		if (point.getY() + 1 < navMesh.get(0).size() - 1) {
 			newArea.add(new Point(point.getX(), point.getY() + 1));
 		}
 		return newArea;
 	}
 
 	private ArrayList<Point> getNeighbours(Point point) {
 		ArrayList<Point> newArea = new ArrayList<>();
 		if (point.getX() - 1 > 0) {
 			newArea.add(new Point(point.getX() - 1, point.getY()));
 		}
 		if (point.getY() - 1 > 0) {
 			newArea.add(new Point(point.getX(), point.getY() - 1));
 		}
 		if (point.getX() - 1 > 0 && point.getY() - 1 > 0) {
 			newArea.add(new Point(point.getX() - 1, point.getY() - 1));
 		}
 		if (point.getX() - 1 > 0
 				&& point.getY() + 1 < navMesh.get(0).size() - 1) {
 			newArea.add(new Point(point.getX() - 1, point.getY() + 1));
 		}
 		if (point.getX() + 1 < navMesh.size() - 1) {
 			newArea.add(new Point(point.getX() + 1, point.getY()));
 		}
 		if (point.getY() + 1 < navMesh.get(0).size() - 1) {
 			newArea.add(new Point(point.getX(), point.getY() + 1));
 		}
 		if (point.getX() + 1 < navMesh.size() - 1
 				&& point.getY() + 1 < navMesh.get(0).size() - 1) {
 			newArea.add(new Point(point.getX() + 1, point.getY() + 1));
 		}
 		if (point.getX() + 1 < navMesh.size() - 1 && point.getY() - 1 > 0) {
 			newArea.add(new Point(point.getX() + 1, point.getY() - 1));
 		}
 		return newArea;
 	}
 }
