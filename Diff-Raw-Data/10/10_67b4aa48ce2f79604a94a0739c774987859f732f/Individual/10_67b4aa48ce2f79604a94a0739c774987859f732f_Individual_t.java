 package ifmo.ru.eugene.luckyanets.OARobot;
 
 import ifmo.ru.eugene.luckyanets.OARobot.Crossover.CrossoverTypes;
 
 import java.io.PrintWriter;
 import java.util.Arrays;
 
 /**
  * Одна особь -- автомат.
  * 
  * @author eugene
  */
 public class Individual implements Comparable<Individual> {
 	/**
 	 * Состояния автомата
 	 */
 	private State[] states;
 
 	/**
 	 * Номер стартового состояния
 	 */
 	private int startState;
 
 	/**
 	 * Значение фатнес-функции
 	 */
 	private double fitness;
 
 	/**
 	 * Стандартный метод для сравнения двух особей. Особи сравниваются на основе
 	 * значения их фитнесс-функций
 	 */
 	@Override
 	public int compareTo(Individual other) {
 		if (this.getFitness() < other.getFitness()) {
			return 1;
 		} else {
 			if (this.getFitness() == other.getFitness())
 				return 0;
 			else
				return -1;
 		}
 	}
 
 	/**
 	 * Простой конструктор
 	 */
 	public Individual() {
 		states = null;
 		startState = -1;
 		fitness = -1;
 	}
 
 	/**
 	 * Выводит на консоль полное описание автомата
 	 */
 	public void print() {
 		System.out.println(String.format("Fitness Function: %f", getFitness()));
 		System.out
 				.println(String.format("Number of states: %d", states.length));
 		System.out.println(String.format("Initial state: %d", startState));
 		for (int i = 0; i < states.length; i++) {
 			System.out.println(String.format("\tState: %d", i));
 			System.out.println(String.format(
 					"\tActions with/without wall forward: %s, %s",
 					states[i].getAction(Forward.Wall),
 					states[i].getAction(Forward.NotWall)));
 			System.out.println(String.format(
 					"\tTransfers with/without wall forward: %s, %s",
 					states[i].getTransfer(Forward.Wall),
 					states[i].getTransfer(Forward.NotWall)));
 		}
 	}
 
 	/**
 	 * Выводит автомат в {@link PrintWriter}
 	 * 
 	 * @param out
 	 *            {@link PrintWriter}
 	 */
 	public void print(PrintWriter out) {
 		out.println(String.format("Fitness Function: %f", fitness));
 		out.println(String.format("Number of states: %d", states.length));
 		out.println(String.format("Initial state: %d", startState));
 		for (int i = 0; i < states.length; i++) {
 			out.println(String.format("\tState: %d", i));
 			out.println("\t\tActions with/without wall forward: "
 					+ states[i].getAction(Forward.Wall) + ' '
 					+ states[i].getAction(Forward.NotWall));
 			out.println(String.format(
 					"\t\tTransfers with/without wall forward: %d, %d",
 					states[i].getTransfer(Forward.Wall),
 					states[i].getTransfer(Forward.NotWall)));
 		}
 		out.flush();
 	}
 
 	/**
 	 * Генерирует новый случайный автомат с заданным числом состояний
 	 * 
 	 * @param size
 	 *            число состояний
 	 */
 	public void genRandom(int size) {
 		states = new State[size];
 		for (int i = 0; i < size; i++) {
 			states[i] = new State(size);
 		}
 		startState = (int) Math.floor(Math.random() * size);
 		fitness = -1;
 	}
 
 	/**
 	 * Скрещивание с другой особью
 	 * 
 	 * @param other
 	 *            с какой особью происходит скрещивание
 	 * @param crossoverType
 	 *            какой тип кроссовера применяется
 	 */
 
 	public void crossTo(Individual other, CrossoverTypes crossoverType) {
 		this.fitness = -1;
 
 		switch (crossoverType) {
 		case DefaultCrossover:
 			crossToDefault(other);
 			break;
 		case EliteCrossover:
 			crossToElete(other);
 			other.fitness = -1;
 			break;
 		case StateCrossover:
 			crossToState(other);
 			other.fitness = -1;
 			break;
 		default:
 			System.err.println("Unknown type of crossover");
 			break;
 		}
 	}
 
 	/**
 	 * {@link CrossoverTypes#EliteCrossover «Элитичный»} кроссовер
 	 * 
 	 * @param eleteOther
 	 *            особь из «элиты», с которой производится скрещивание
 	 */
 	private void crossToElete(Individual eleteOther) {
 		/*
 		 * var -- какой вариант скрещивания применяется: 0 -- ничего не
 		 * меняется; 1 -- особи меняются действиями и переходами, при условии,
 		 * что впереди стена; 2 -- особи меняются действиями и переходами, при
 		 * условии, что впереди нет стены; 3 -- особи меняются обоими действиями
 		 * и переходами
 		 */
 		int var;
 		for (int i = 0; i < states.length; i++) {
 			var = (int) Math.floor(Math.random() * 4);
 			switch (var) {
 			case 0:
 				continue;
 			case 1:
 				copyActionAndTransferFrom(eleteOther, i, Forward.Wall);
 				break;
 			case 2:
 				copyActionAndTransferFrom(eleteOther, i, Forward.NotWall);
 				break;
 			case 3:
 				copyActionAndTransferFrom(eleteOther, i, Forward.Wall);
 				copyActionAndTransferFrom(eleteOther, i, Forward.NotWall);
 				break;
 			default:
 				System.err
 						.println("Strange case in the process of «elete» crossover");
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Копирует в конкретном состоянии действие(-я) и переход(-ы) у другой
 	 * «элитной» особи
 	 * 
 	 * @param eleteOther
 	 *            элитная особь
 	 * @param i
 	 *            номер состояния, по которому будет проихводиться копирование
 	 * @param f
 	 *            по какому сигналу производится копирование (есть ли впереди
 	 *            стена)
 	 */
 	private void copyActionAndTransferFrom(Individual eleteOther, int i,
 			Forward f) {
 		this.states[i].setAction(f, eleteOther.states[i].getAction(f));
 		this.states[i].setTransfer(f, eleteOther.states[i].getTransfer(f));
 	}
 
 	/**
 	 * {@link CrossoverTypes#DefaultCrossover Стандартный} кроссовер
 	 * 
 	 * @param other
 	 *            другая особь
 	 */
 	private void crossToDefault(Individual other) {
 		/*
 		 * var -- какой вариант скрещивания применяется: 0 -- ничего не
 		 * меняется; 1 -- особи меняются действиями и переходами, при условии,
 		 * что впереди стена; 2 -- особи меняются действиями и переходами, при
 		 * условии, что впереди нет стены; 3 -- особи меняются обоими действиями
 		 * и переходами
 		 */
 		int var;
 		for (int i = 0; i < states.length; i++) {
 			var = (int) Math.floor(Math.random() * 4);
 			switch (var) {
 			case 0:
 				continue;
 			case 1:
 				exchangeActionAndTransferTo(other, i, Forward.Wall);
 				break;
 			case 2:
 				exchangeActionAndTransferTo(other, i, Forward.NotWall);
 				break;
 			case 3:
 				exchangeActionAndTransferTo(other, i, Forward.Wall);
 				exchangeActionAndTransferTo(other, i, Forward.NotWall);
 				break;
 			default:
 				System.err
 						.println("Strange case in the process of default crossover");
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Меняет у двух особей в конкретном состоянии действие(-я) и переход(-ы)
 	 * 
 	 * @param other
 	 *            с какой особью меняется данная особь
 	 * @param i
 	 *            в каком состоянии происходит обмен
 	 * @param f
 	 *            по какому сигналу производится обмен (есть ли впереди стена)
 	 */
 	private void exchangeActionAndTransferTo(Individual other, int i, Forward f) {
 		Action a = this.states[i].getAction(f);
 		int tr = this.states[i].getTransfer(f);
 		this.states[i].setAction(f, other.states[i].getAction(f));
 		this.states[i].setTransfer(f, other.states[i].getTransfer(f));
 		other.states[i].setAction(f, a);
 		other.states[i].setTransfer(f, tr);
 	}
 
 	/**
 	 * Кроссовер {@link CrossoverTypes#StateCrossover «по состояниям»}
 	 * 
 	 * @param other
 	 *            с какой особью делается скрещивание
 	 */
 	private void crossToState(Individual other) {
 		int firstStateNumber = (int) (Math.random() * this.states.length);
 		int secondStateNumber = (int) (Math.random() * this.states.length);
 		State.echangeStates(this.states[firstStateNumber],
 				other.states[secondStateNumber]);
 	}
 
 	/**
 	 * Мутация особи
 	 */
 	public void mutate() {
 		this.fitness = -1;
 		/*
 		 * var -- какой вариант мутации: 0 -- меняем стартовое состояние на
 		 * случайное; 1 -- у случайного состояния меняем действие на случайном
 		 * переходе; 2 -- у случайного состояния меняем конец какого-то
 		 * перехода; 3 -- у случайного состояния меняем местами концы двух
 		 * переходов; 4 -- сгенерировать новую особь
 		 */
 		int var = (int) Math.floor(Math.random() * 5);
 		int state = (int) Math.floor(Math.random() * states.length), t = (int) Math
 				.floor(Math.random() * 2);
 		Forward f = (t == 0 ? Forward.Wall : Forward.NotWall);
 		switch (var) {
 		case 0:
 			startState = (int) Math.floor(Math.random() * states.length);
 			break;
 		case 1:
 			Action a = states[state].genRandAction();
 			states[state].setAction(f, a);
 			break;
 		case 2:
 			int newTo = (int) Math.floor(Math.random() * states.length);
 			states[state].setTransfer(f, newTo);
 			break;
 		case 3:
 			exchangeWallNotWall(state);
 			break;
 		case 4:
 			genRandom(states.length);
 			break;
 		default:
 			System.err.println("Something wrong with mutation");
 			break;
 		}
 	}
 
 	/**
 	 * Меняет переходы в случаях, когда впереди есть стена и когда нет, местами
 	 * 
 	 * @param s
 	 *            номер особи
 	 */
 	private void exchangeWallNotWall(int s) {
 		Action aWall = states[s].getAction(Forward.Wall);
 		int trWall = states[s].getTransfer(Forward.Wall);
 
 		states[s].setAction(Forward.Wall, states[s].getAction(Forward.NotWall));
 		states[s].setTransfer(Forward.Wall,
 				states[s].getTransfer(Forward.NotWall));
 
 		states[s].setAction(Forward.NotWall, aWall);
 		states[s].setTransfer(Forward.NotWall, trWall);
 	}
 
 	/**
 	 * Вычисляет новую координату по X
 	 * 
 	 * @param d
 	 *            направление, в котором производится движение
 	 * @param oldX
 	 *            старое значение координаты по X
 	 * @return новое значение координаты по Y
 	 */
 	private int goX(Direction d, int oldX) {
 		switch (d) {
 		case Left:
 			return (oldX - 1);
 		case Right:
 			return (oldX + 1);
 		case Down:
 			return oldX;
 		case Up:
 			return oldX;
 		default:
 			System.err.println("Something wrong with moving (X)");
 			return oldX;
 		}
 	}
 
 	/**
 	 * Вычисляет новую координату по Y
 	 * 
 	 * @param d
 	 *            направление, в котором производится движение
 	 * @param oldY
 	 *            старое значение координаты по Y
 	 * @return новое значение координаты по Y
 	 */
 	private int goY(Direction d, int oldY) {
 		switch (d) {
 		case Up:
 			return (oldY - 1);
 		case Down:
 			return (oldY + 1);
 		case Left:
 			return oldY;
 		case Right:
 			return oldY;
 		default:
 			System.err.println("Something wrong with moving (Y)");
 			return oldY;
 		}
 	}
 
 	/**
 	 * Вычисляет новое направление
 	 * 
 	 * @param d
 	 *            старое направление
 	 * @param a
 	 *            действие, которое производится
 	 * @return новое направление
 	 */
 	private Direction getNewDir(Direction d, Action a) {
 		switch (a) {
 		case MoveForvard:
 			return d;
 		case DoNothing:
 			return d;
 		case TurnLeft:
 			switch (d) {
 			case Up:
 				return Direction.Left;
 			case Down:
 				return Direction.Right;
 			case Right:
 				return Direction.Up;
 			case Left:
 				return Direction.Down;
 			default:
 				System.err
 						.println("Something wrong with getting new direction (turn left)");
 				break;
 			}
 		case TurnRight:
 			switch (d) {
 			case Up:
 				return Direction.Right;
 			case Down:
 				return Direction.Left;
 			case Right:
 				return Direction.Down;
 			case Left:
 				return Direction.Up;
 			default:
 				System.err
 						.println("Something wrong with getting new direction (turn right)");
 				break;
 			}
 		default:
 			System.err
 					.println("Something wrong with getting new direction (globally)");
 			break;
 		}
 		return d;
 	}
 
 	/**
 	 * Считает фитнесс-функцию для данной особи
 	 */
 	public void calculateFitness() {
 		fitness = 0;
 		int numberOfSteps = 0;
 		PlayingField field = TestWorker.getField();
 		int posX = field.getStartX();
 		int posY = field.getStartY();
		double startDistance = (Math.abs(posX - field.getEndX()) + Math
				.abs(posY - field.getEndY())) * 1.0;
 		int curState = startState, newX, newY;
 		Direction curDir = field.getStartDir();
 		while (numberOfSteps < (int) (1.5 * Main.numberOfStepsInGame)) {
 			if (numberOfSteps == Main.numberOfStepsInGame) {
 				fitness = (Math.abs(posX - field.getEndX()) + Math.abs(posY
 						- field.getEndY())) * 1.0;
 			}
 			if (field.isEnd(posX, posY))
 				break;
 			newX = goX(curDir, posX);
 			newY = goY(curDir, posY);
 			Forward f = field.checkIfWall(newX, newY);
 			int newState = states[curState].getTransfer(f);
 			Action toDo = states[curState].getAction(f);
 			curState = newState;
 			if (toDo == Action.MoveForvard) {
 				if (f == Forward.NotWall) {
 					posX = newX;
 					posY = newY;
 				}
 			}
 			curDir = getNewDir(curDir, toDo);
 			numberOfSteps++;
 		}
 		fitness += numberOfSteps / 500.0;
		fitness = startDistance + 1 - fitness;
 		return;
 	}
 
 	/**
 	 * Возвращает значение фитнесс-функции. Если оно ещё не вычеслено, вычисляет
 	 * его
 	 * 
 	 * @return значение фитнесс-функции
 	 */
 	public double getFitness() {
 		if (fitness == -1)
 			calculateFitness();
 		return fitness;
 	}
 
 	/**
 	 * Создаёт копию автомата
 	 * 
 	 * @return копия автомата
 	 */
 	public Individual copy() {
 		Individual other = new Individual();
 		other.fitness = this.fitness;
 		other.startState = this.startState;
 		other.states = Arrays.copyOf(states, states.length);
 		return other;
 	}
 }
